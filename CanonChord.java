import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Repräsentiert einen Dreiklang für die Harmonik-Validierung des Kanons.
 *
 * <p>Ein Dreiklang besteht aus Grundton, Terz und Quinte. Unterstützt werden
 * Dur- und Moll-Dreiklänge. Die Klasse prüft, ob ein MIDI-Pitch zu diesem
 * Akkord gehört (Akkordfremde-Ton-Erkennung).</p>
 *
 * <h2>Dreiklang-Intervalle:</h2>
 * <pre>
 *   Dur  (MAJOR): Grundton + 4 Halbtöne + 7 Halbtöne (C–E–G)
 *   Moll (MINOR): Grundton + 3 Halbtöne + 7 Halbtöne (A–C–E)
 * </pre>
 *
 * @author CanonMaker
 * @version 1.0
 */
public final class CanonChord {

    /**
     * Dreiklang-Typ.
     */
    public enum Type {
        /** Dur-Dreiklang: Grundton + große Terz (4 HT) + Quinte (7 HT) */
        MAJOR,
        /** Moll-Dreiklang: Grundton + kleine Terz (3 HT) + Quinte (7 HT) */
        MINOR
    }

    private final String    root;   // Deutscher Notenname des Grundtons
    private final Type      type;
    private final int       rootPitchClass; // 0–11
    private final Set<Integer> chordTones; // Halbton-Klassen 0–11

    /**
     * Erzeugt einen Dreiklang.
     *
     * @param germanRoot Deutscher Notenname des Grundtons (C, D, E, F, G, A, H, Fis, …)
     * @param type       {@link Type#MAJOR} oder {@link Type#MINOR}
     * @throws IllegalArgumentException wenn der Notenname unbekannt ist
     */
    public CanonChord(String germanRoot, Type type) {
        if (!CanonNote.isKnownName(germanRoot)) {
            throw new IllegalArgumentException("Unbekannter Akkord-Grundton: " + germanRoot);
        }
        this.root           = germanRoot;
        this.type           = type;
        this.rootPitchClass = CanonNote.computePitch(germanRoot, 0) % 12;

        Set<Integer> tones = new HashSet<>();
        tones.add(rootPitchClass);
        int thirdInterval = (type == Type.MAJOR) ? 4 : 3;
        tones.add((rootPitchClass + thirdInterval) % 12);
        tones.add((rootPitchClass + 7) % 12);
        this.chordTones = Collections.unmodifiableSet(tones);
    }

    /**
     * Gibt die Halbton-Klassen (0–11) der drei Akkordt öne zurück.
     *
     * @return Unveränderliches Set mit genau 3 Einträgen (0–11)
     */
    public Set<Integer> getChordTones() {
        return chordTones;
    }

    /**
     * Prüft ob ein MIDI-Pitch (beliebige Oktave) ein Akkordton ist.
     *
     * @param midiPitch MIDI-Pitch (0–127)
     * @return {@code true} wenn der Pitch zur Akkordklasse gehört
     */
    public boolean isChordTone(int midiPitch) {
        return chordTones.contains(((midiPitch % 12) + 12) % 12);
    }

    /** @return Deutschen Grundton-Namen */
    public String getRoot() { return root; }

    /** @return Dreiklang-Typ */
    public Type getType() { return type; }

    @Override
    public String toString() {
        return root + " " + type.name() + " " + chordTones;
    }
}
