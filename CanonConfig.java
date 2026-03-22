/**
 * Konfigurationsparameter für den Kanon-Aufbau.
 *
 * <p>Legt fest, wie viele Stimmen der Kanon hat, wie groß der Zeitversatz
 * zwischen den Einsätzen ist, welche Instrumente und MIDI-Kanäle verwendet
 * werden und wie oft die Melodie pro Stimme wiederholt wird.</p>
 *
 * <h2>Typische Einstellung für einen 3-stimmigen Kanon mit 3 Takten Versatz:</h2>
 * <pre>{@code
 * CanonConfig config = new CanonConfig()
 *     .setNumVoices(3)
 *     .setOffsetTicks(CanonNote.WHOLE * 4)  // 1 Takt = 4 Viertelnoten
 *     .setInstruments(new int[]{0, 40, 42}) // Klavier, Violine, Cello
 *     .setChannels(new int[]{0, 1, 2})
 *     .setTempoBpm(80)
 *     .setRepetitions(2);
 * }</pre>
 *
 * @author CanonMaker
 * @version 1.0
 */
public final class CanonConfig {

    private int   numVoices    = 3;
    private int   offsetTicks  = CanonNote.WHOLE;        // 1 Takt (4 Viertelnoten = 1920 Ticks)
    private int[] instruments  = {0, 40, 42};            // Klavier, Violine, Cello
    private int[] channels     = {0, 1, 2};
    private int   tempoBpm     = 80;
    private int   repetitions  = 2;
    private int[] octaveOffsets = {};                    // Oktavversatz pro Stimme (Standard: 0)

    /** Anzahl der Stimmen (Standard: 3). */
    public CanonConfig setNumVoices(int numVoices) {
        if (numVoices < 2) throw new IllegalArgumentException("Mindestens 2 Stimmen nötig");
        this.numVoices = numVoices;
        return this;
    }

    /**
     * Zeitversatz zwischen den Stimmen in Ticks.
     * <p>Standard: 1920 ticks = 1 Takt (bei 4/4 mit Viertelnoten à 480 ticks).</p>
     */
    public CanonConfig setOffsetTicks(int offsetTicks) {
        if (offsetTicks <= 0) throw new IllegalArgumentException("Offset muss > 0 sein");
        this.offsetTicks = offsetTicks;
        return this;
    }

    /**
     * GM-Programm-Nummern (0-basiert) für jede Stimme.
     * Das Array muss mindestens so lang sein wie {@link #getNumVoices()}.
     */
    public CanonConfig setInstruments(int[] instruments) {
        this.instruments = instruments;
        return this;
    }

    /**
     * MIDI-Kanäle (0–15) für jede Stimme.
     * Das Array muss mindestens so lang sein wie {@link #getNumVoices()}.
     */
    public CanonConfig setChannels(int[] channels) {
        this.channels = channels;
        return this;
    }

    /** Tempo in BPM (70–90 laut Kanon-Regeln). Standard: 80. */
    public CanonConfig setTempoBpm(int tempoBpm) {
        if (tempoBpm < 1) throw new IllegalArgumentException("Tempo muss >= 1 sein");
        this.tempoBpm = tempoBpm;
        return this;
    }

    /**
     * Oktavversatz pro Stimme (scaleMaker-Einheiten, 0 = keine Verschiebung).
     * <p>Beispiel: {@code new int[]{0, 0, -1}} lässt Stimme 3 eine Oktave tiefer spielen,
     * sodass Notationsprogramme (z.B. Dorian) automatisch den Bassschlüssel wählen.</p>
     */
    public CanonConfig setOctaveOffsets(int[] octaveOffsets) {
        this.octaveOffsets = octaveOffsets;
        return this;
    }

    /** Anzahl der Melodie-Wiederholungen pro Stimme (Standard: 2). */
    public CanonConfig setRepetitions(int repetitions) {
        if (repetitions < 1) throw new IllegalArgumentException("Mindestens 1 Wiederholung");
        this.repetitions = repetitions;
        return this;
    }

    // ==================== GETTER ====================

    public int   getNumVoices()    { return numVoices; }
    public int   getOffsetTicks()  { return offsetTicks; }
    public int[] getInstruments()  { return instruments; }
    public int[] getChannels()     { return channels; }
    public int   getTempoBpm()     { return tempoBpm; }
    public int   getRepetitions()  { return repetitions; }
    public int[] getOctaveOffsets(){ return octaveOffsets; }

    /** Instrument-Programmnummer für Stimme v (0-basiert), mit Fallback auf 0. */
    public int getInstrumentFor(int voice) {
        return (voice < instruments.length) ? instruments[voice] : 0;
    }

    /** MIDI-Kanal für Stimme v (0-basiert), mit Fallback auf v. */
    public int getChannelFor(int voice) {
        return (voice < channels.length) ? channels[voice] : voice;
    }

    /** Oktavversatz für Stimme v (0-basiert), mit Fallback auf 0. */
    public int getOctaveOffsetFor(int voice) {
        return (voice < octaveOffsets.length) ? octaveOffsets[voice] : 0;
    }

    @Override
    public String toString() {
        return "CanonConfig{voices=" + numVoices
               + ", offset=" + offsetTicks + " ticks"
               + ", tempo=" + tempoBpm + " BPM"
               + ", repetitions=" + repetitions + "}";
    }
}
