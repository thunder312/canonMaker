import java.util.HashMap;
import java.util.Map;

/**
 * Repräsentiert eine einzelne Note (oder Pause) innerhalb einer Kanon-Melodie.
 *
 * <p>Eine Note wird durch den deutschen Notennamen, die scaleMaker-Oktave und die
 * Dauer in MIDI-Ticks definiert. Der MIDI-Pitch wird beim Anlegen berechnet und
 * intern gespeichert. Pausen werden durch {@link #rest(int)} erzeugt.</p>
 *
 * <h2>Oktav-Konvention (identisch mit midiMaker / scaleMaker):</h2>
 * <pre>
 *   scaleMaker octave 0 → C4 = MIDI 60 (mittleres C)
 *   scaleMaker octave 1 → C5 (eingestrichene Oktave, c')
 *   scaleMaker octave -1 → C3 (kleine Oktave)
 * </pre>
 *
 * <h2>Stimmumfang für Sing-Stimmen:</h2>
 * <pre>
 *   Minimum: kleines G = G3 = MIDI 55 (scaleMaker octave -1)
 *   Maximum: e'' = E5   = MIDI 76 (scaleMaker octave 1)
 * </pre>
 *
 * <h2>Ticks-Konstanten (480 Ticks pro Viertelnote):</h2>
 * <pre>
 *   WHOLE = 1920, DOTTED_HALF = 1440, HALF = 960, DOTTED_QUARTER = 720,
 *   QUARTER = 480, DOTTED_EIGHTH = 360, EIGHTH = 240, SIXTEENTH = 120
 * </pre>
 *
 * @author CanonMaker
 * @version 1.0
 */
public final class CanonNote {

    // ==================== TICKS-KONSTANTEN ====================

    /** Ganze Note (4 Schläge) */
    public static final int WHOLE          = 1920;
    /** Punktierte halbe Note (3 Schläge) */
    public static final int DOTTED_HALF    = 1440;
    /** Halbe Note (2 Schläge) */
    public static final int HALF           = 960;
    /** Punktierte Viertelnote (1,5 Schläge) */
    public static final int DOTTED_QUARTER = 720;
    /** Viertelnote (1 Schlag) */
    public static final int QUARTER        = 480;
    /** Punktierte Achtelnote (0,75 Schläge) */
    public static final int DOTTED_EIGHTH  = 360;
    /** Achtelnote (0,5 Schläge) */
    public static final int EIGHTH         = 240;
    /** Sechzehntelnote (0,25 Schläge) */
    public static final int SIXTEENTH      = 120;

    // ==================== STIMMUMFANG ====================

    /** Minimaler MIDI-Pitch für Singstimme: kleines G = G3 = MIDI 55 */
    public static final int VOCAL_MIN_PITCH = 55;
    /** Maximaler MIDI-Pitch für Singstimme: e'' = E5 = MIDI 76 */
    public static final int VOCAL_MAX_PITCH = 76;

    // ==================== CHROMATISCHE HALBTONTABELLE ====================

    private static final Map<String, Integer> CHROMATIC = new HashMap<>();

    static {
        // Stammtöne
        CHROMATIC.put("C",   0);  CHROMATIC.put("D",   2);  CHROMATIC.put("E",   4);
        CHROMATIC.put("F",   5);  CHROMATIC.put("G",   7);  CHROMATIC.put("A",   9);
        CHROMATIC.put("H",  11);  // H = B natural (deutsch)
        // Kreuze
        CHROMATIC.put("Cis", 1);  CHROMATIC.put("Dis", 3);  CHROMATIC.put("Eis", 5);
        CHROMATIC.put("Fis", 6);  CHROMATIC.put("Gis", 8);  CHROMATIC.put("Ais", 10);
        CHROMATIC.put("His", 0);  // enharmonisch = C der nächsten Oktave
        // Be (deutsch: B = B♭)
        CHROMATIC.put("B",  10);  CHROMATIC.put("Des", 1);  CHROMATIC.put("Es",  3);
        CHROMATIC.put("Ges", 6);  CHROMATIC.put("As",  8);
    }

    // ==================== FELDER ====================

    /** Deutscher Notenname, oder {@code null} bei Pausen. */
    private final String germanName;
    /** scaleMaker-Oktave (0 = Standardlage, C4). Irrelevant bei Pausen. */
    private final int octave;
    /** Dauer in MIDI-Ticks. */
    private final int ticks;
    /** Berechneter MIDI-Pitch (0–127), oder -1 bei Pausen. */
    private final int midiPitch;

    // ==================== FACTORY-METHODEN ====================

    /**
     * Erzeugt eine Note.
     *
     * @param germanName       Deutscher Notenname (C, Cis, D, ..., H, B)
     * @param scaleMakerOctave Oktavlage (0 = C4, -1 = C3, 1 = C5)
     * @param ticks            Dauer in Ticks (z.B. {@link #QUARTER})
     * @return Neue Note
     * @throws IllegalArgumentException wenn der Notenname unbekannt ist
     */
    public static CanonNote of(String germanName, int scaleMakerOctave, int ticks) {
        return new CanonNote(germanName, scaleMakerOctave, ticks);
    }

    /**
     * Erzeugt eine Pause.
     *
     * @param ticks Dauer in Ticks
     * @return Neue Pause
     */
    public static CanonNote rest(int ticks) {
        return new CanonNote(null, 0, ticks);
    }

    // ==================== KONSTRUKTOR ====================

    private CanonNote(String germanName, int scaleMakerOctave, int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("Ticks müssen > 0 sein: " + ticks);
        this.germanName = germanName;
        this.octave     = scaleMakerOctave;
        this.ticks      = ticks;
        if (germanName != null) {
            this.midiPitch = computePitch(germanName, scaleMakerOctave);
        } else {
            this.midiPitch = -1;
        }
    }

    // ==================== PITCH-BERECHNUNG ====================

    /**
     * Berechnet den MIDI-Pitch aus deutschem Notennamen und scaleMaker-Oktave.
     * Formel identisch mit {@code NoteConverter.germanNameToMidiPitch} aus midiMaker.
     */
    static int computePitch(String germanName, int scaleMakerOctave) {
        Integer offset = CHROMATIC.get(germanName);
        if (offset == null) {
            throw new IllegalArgumentException(
                "Unbekannter Notenname: '" + germanName
                + "'. Gültig: C, Cis, Des, D, Dis, Es, E, F, Fis, Ges, G, Gis, As, A, Ais, B, H");
        }
        int midiOctave = scaleMakerOctave + 5;
        return Math.max(0, Math.min(127, midiOctave * 12 + offset));
    }

    /**
     * Gibt alle bekannten deutschen Notennamen zurück (für Validierung).
     */
    static boolean isKnownName(String name) {
        return CHROMATIC.containsKey(name);
    }

    // ==================== GETTER ====================

    /** @return Deutscher Notenname, oder {@code null} bei Pausen */
    public String getGermanName() { return germanName; }

    /** @return scaleMaker-Oktave */
    public int getOctave() { return octave; }

    /** @return Dauer in MIDI-Ticks */
    public int getTicks() { return ticks; }

    /** @return MIDI-Pitch (0–127), oder -1 bei Pausen */
    public int getMidiPitch() { return midiPitch; }

    /** @return {@code true} wenn diese Note eine Pause ist */
    public boolean isRest() { return germanName == null; }

    @Override
    public String toString() {
        if (isRest()) return "Pause(" + ticks + " ticks)";
        return germanName + (octave >= 0 ? "+" : "") + octave
               + "(MIDI " + midiPitch + ", " + ticks + " ticks)";
    }
}
