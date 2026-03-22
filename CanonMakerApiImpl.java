import java.util.List;
import java.util.ServiceLoader;

/**
 * Implementierung der {@link CanonMakerApi}.
 *
 * <p>Baut den Kanon durch zeitversetztes Wiederholen der Melodie in mehreren Stimmen
 * und exportiert ihn über das midiMaker-Plugin als MIDI-Typ-1-Datei.</p>
 *
 * <p>Das midiMaker-Plugin wird intern über den {@link ServiceLoader} geladen;
 * {@code midimaker.jar} muss im Classpath vorhanden sein.</p>
 *
 * @author CanonMaker
 * @version 1.0
 */
class CanonMakerApiImpl implements CanonMakerApi {

    // ==================== MELODIE ERSTELLEN ====================

    @Override
    public CanonMelody createMelody(String title) {
        return new CanonMelody(title);
    }

    @Override
    public void addNote(CanonMelody melody, String germanName, int scaleMakerOctave, int ticks) {
        melody.addNote(CanonNote.of(germanName, scaleMakerOctave, ticks));
    }

    @Override
    public void addRest(CanonMelody melody, int ticks) {
        melody.addNote(CanonNote.rest(ticks));
    }

    @Override
    public void addChordAnnotation(CanonMelody melody, int beatIndex,
                                   String germanRoot, CanonChord.Type type, int durationBeats) {
        melody.addChordAnnotation(beatIndex, new CanonChord(germanRoot, type), durationBeats);
    }

    // ==================== VALIDIERUNG ====================

    @Override
    public List<CanonViolation> validateMelody(CanonMelody melody) {
        return CanonValidator.validateMelody(melody);
    }

    @Override
    public List<CanonViolation> validateCanon(CanonMelody melody, CanonConfig config) {
        return CanonValidator.validateCanon(melody, config);
    }

    // ==================== EXPORT ====================

    @Override
    public void exportCanon(CanonMelody melody, CanonConfig config, String filePath)
            throws Exception {

        MidiMakerApi midi = loadMidiMaker();

        MidiComposition composition = midi.createComposition(melody.getTitle(), config.getTempoBpm());
        composition.setTimeSignature(4, 4);

        int numVoices   = config.getNumVoices();
        int offsetTicks = config.getOffsetTicks();
        int repetitions = config.getRepetitions();

        for (int v = 0; v < numVoices; v++) {

            // Instrument per GM-Programmnummer suchen
            MidiInstrument instr = findInstrument(config.getInstrumentFor(v));
            int channel = config.getChannelFor(v);
            MidiTrack track = midi.createTrack(instr, channel, "Stimme " + (v + 1));

            // Anlauf-Pause für Stimmen 2, 3, …
            int delayTicks = v * offsetTicks;
            if (delayTicks > 0) {
                addTicksAsRests(track, delayTicks);
            }

            // Melodie wiederholen
            int octaveOffset = config.getOctaveOffsetFor(v);
            for (int rep = 0; rep < repetitions; rep++) {
                for (CanonNote note : melody.getNotes()) {
                    NoteDuration dur = ticksToNoteDuration(note.getTicks());
                    if (note.isRest()) {
                        track.addRest(dur);
                    } else {
                        track.addNote(midi.fromNoteName(note.getGermanName(),
                                                        note.getOctave() + octaveOffset, dur));
                    }
                }
            }

            composition.addTrack(track);
        }

        midi.writeToFile(composition, filePath);
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    // ==================== HILFSMETHODEN ====================

    /**
     * Lädt das midiMaker-Plugin über den ServiceLoader.
     */
    private MidiMakerApi loadMidiMaker() {
        ServiceLoader<MidiMakerPlugin> loader = ServiceLoader.load(MidiMakerPlugin.class);
        MidiMakerPlugin plugin = loader.findFirst()
            .orElseThrow(() -> new RuntimeException(
                "midiMaker-Plugin nicht gefunden! midimaker.jar muss im Classpath liegen."));
        plugin.initialize();
        return plugin.getApi();
    }

    /**
     * Sucht ein {@link MidiInstrument} nach GM-Programmnummer (0-basiert).
     * Gibt {@link MidiInstrument#ACOUSTIC_GRAND_PIANO} als Fallback zurück.
     */
    private MidiInstrument findInstrument(int programNumber) {
        for (MidiInstrument instr : MidiInstrument.values()) {
            if (instr.getProgramNumber() == programNumber) return instr;
        }
        return MidiInstrument.ACOUSTIC_GRAND_PIANO;
    }

    /**
     * Zerlegt eine Tick-Anzahl (Anlauf-Pause) in Standard-NoteDuration-Pausen.
     * Greedy: größtmögliche Notenwerte zuerst.
     */
    private void addTicksAsRests(MidiTrack track, int ticks) {
        NoteDuration[] bySize = {
            NoteDuration.WHOLE, NoteDuration.DOTTED_HALF, NoteDuration.HALF,
            NoteDuration.DOTTED_QUARTER, NoteDuration.QUARTER, NoteDuration.DOTTED_EIGHTH,
            NoteDuration.EIGHTH, NoteDuration.SIXTEENTH, NoteDuration.THIRTY_SECOND
        };
        while (ticks > 0) {
            for (NoteDuration d : bySize) {
                if (d.getTicks() <= ticks) {
                    track.addRest(d);
                    ticks -= d.getTicks();
                    break;
                }
            }
        }
    }

    /**
     * Konvertiert eine Tick-Anzahl in den exakt passenden {@link NoteDuration}-Wert.
     *
     * @throws IllegalArgumentException wenn der Wert keiner Standarddauer entspricht
     */
    private NoteDuration ticksToNoteDuration(int ticks) {
        for (NoteDuration d : NoteDuration.values()) {
            if (d.getTicks() == ticks) return d;
        }
        throw new IllegalArgumentException(
            "Tick-Wert " + ticks + " entspricht keiner Standard-NoteDuration. "
            + "Verwende CanonNote-Konstanten (WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH, ...).");
    }
}
