import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repräsentiert die Grundmelodie eines Kanons.
 *
 * <p>Eine {@code CanonMelody} ist eine Folge von {@link CanonNote}-Objekten
 * (Noten und Pausen), ergänzt durch optionale Akkord-Annotationen für die
 * Harmonie-Validierung.</p>
 *
 * <p>Die Melodie wird von {@link CanonMakerApiImpl} verwendet, um durch zeitversetztes
 * Wiederholen in mehreren Stimmen einen Kanon zu erzeugen.</p>
 *
 * <h2>Akkord-Annotationen:</h2>
 * <p>Über {@link #addChordAnnotation(int, CanonChord, int)} können Akkorde für
 * bestimmte Schlag-Positionen hinterlegt werden. Der Validator prüft dann,
 * ob die Dreiklangstöne auf Schlag 1 und 3 vollständig vorhanden sind.</p>
 *
 * @author CanonMaker
 * @version 1.0
 */
public final class CanonMelody {

    /**
     * Ordnet einen Akkord einer Beat-Position innerhalb der Melodie zu.
     */
    public static final class ChordAnnotation {
        /** Beat-Index (0-basiert; beat 0 = Zählzeit 1, beat 2 = Zählzeit 3) */
        public final int beatIndex;
        /** Dauer in Beats */
        public final int durationBeats;
        /** Der Dreiklang */
        public final CanonChord chord;

        ChordAnnotation(int beatIndex, CanonChord chord, int durationBeats) {
            this.beatIndex     = beatIndex;
            this.chord         = chord;
            this.durationBeats = durationBeats;
        }
    }

    private static final int TICKS_PER_BEAT = CanonNote.QUARTER; // 480

    private final String title;
    private final List<CanonNote>         notes       = new ArrayList<>();
    private final List<ChordAnnotation>   chordAnnotations = new ArrayList<>();

    /**
     * Erstellt eine neue Melodie.
     *
     * @param title Bezeichnung / Titel der Melodie
     */
    public CanonMelody(String title) {
        this.title = title != null ? title : "";
    }

    // ==================== NOTEN HINZUFÜGEN ====================

    /**
     * Fügt eine Note ans Ende der Melodie an.
     *
     * @param note Die hinzuzufügende Note oder Pause
     */
    public void addNote(CanonNote note) {
        if (note == null) throw new IllegalArgumentException("Note darf nicht null sein");
        notes.add(note);
    }

    // ==================== AKKORD-ANNOTATIONEN ====================

    /**
     * Annotiert einen Akkord für eine bestimmte Beat-Position.
     *
     * <p>Beispiel: Beat 0 → C-Dur für 4 Beats (= ein ganzer 4/4-Takt).</p>
     *
     * @param beatIndex    Beat-Index (0-basiert)
     * @param chord        Dreiklang
     * @param durationBeats Dauer in Viertelnote-Einheiten
     */
    public void addChordAnnotation(int beatIndex, CanonChord chord, int durationBeats) {
        if (chord == null) throw new IllegalArgumentException("Akkord darf nicht null sein");
        chordAnnotations.add(new ChordAnnotation(beatIndex, chord, durationBeats));
    }

    // ==================== ABFRAGEN ====================

    /**
     * Gibt den Akkord zurück, der für den angegebenen Beat-Index annotiert wurde.
     * Gibt {@code null} zurück, wenn keine Annotation vorhanden ist.
     *
     * @param beatIndex Beat-Index (0-basiert innerhalb der Melodie)
     * @return Akkord oder {@code null}
     */
    public CanonChord getChordAtBeat(int beatIndex) {
        for (ChordAnnotation ca : chordAnnotations) {
            if (beatIndex >= ca.beatIndex && beatIndex < ca.beatIndex + ca.durationBeats) {
                return ca.chord;
            }
        }
        return null;
    }

    /**
     * Gibt die Note zurück, die zum angegebenen Tick-Offset aktiv ist.
     * Der Tick wird innerhalb eines Melodie-Durchlaufs berechnet.
     *
     * @param tickWithinMelody Tick-Position innerhalb der Melodie (0 bis totalTicks-1)
     * @return Aktive Note, oder {@code null} wenn tickWithinMelody außerhalb liegt
     */
    public CanonNote getNoteAtTick(int tickWithinMelody) {
        int cursor = 0;
        for (CanonNote note : notes) {
            if (tickWithinMelody >= cursor && tickWithinMelody < cursor + note.getTicks()) {
                return note;
            }
            cursor += note.getTicks();
        }
        return null;
    }

    /**
     * Gesamtdauer der Melodie in Ticks.
     *
     * @return Summe aller Noten-Ticks
     */
    public int getTotalTicks() {
        return notes.stream().mapToInt(CanonNote::getTicks).sum();
    }

    /**
     * Gesamtdauer der Melodie in Beats (Viertelnoten).
     *
     * @return Anzahl der Beats
     */
    public int getTotalBeats() {
        return getTotalTicks() / TICKS_PER_BEAT;
    }

    /** @return Titel der Melodie */
    public String getTitle() { return title; }

    /** @return Unveränderliche Liste aller Noten */
    public List<CanonNote> getNotes() { return Collections.unmodifiableList(notes); }

    /** @return Unveränderliche Liste aller Akkord-Annotationen */
    public List<ChordAnnotation> getChordAnnotations() {
        return Collections.unmodifiableList(chordAnnotations);
    }

    /** @return Anzahl der Noten/Pausen in der Melodie */
    public int size() { return notes.size(); }

    @Override
    public String toString() {
        return "CanonMelody{title='" + title + "', notes=" + notes.size()
               + ", totalTicks=" + getTotalTicks()
               + ", totalBeats=" + getTotalBeats()
               + ", chordAnnotations=" + chordAnnotations.size() + "}";
    }
}
