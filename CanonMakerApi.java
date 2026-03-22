import java.util.List;

/**
 * API-Interface für alle Kanon-Erzeugungsfunktionen des canonMaker-Plugins.
 *
 * <p>Dieses Interface definiert den vollständigen Funktionsumfang des Plugins.
 * Das Hauptprogramm bezieht eine Instanz über {@link CanonMakerPlugin#getApi()}.</p>
 *
 * <h2>Typischer Workflow:</h2>
 * <pre>{@code
 * CanonMakerApi api = plugin.getApi();
 *
 * // 1. Melodie anlegen
 * CanonMelody melodie = api.createMelody("Mein Kanon");
 * api.addNote(melodie, "C", 0, CanonNote.QUARTER);   // C4, Viertelnote
 * api.addNote(melodie, "E", 0, CanonNote.QUARTER);   // E4
 * api.addNote(melodie, "G", 0, CanonNote.HALF);      // G4, halbe Note
 * api.addChordAnnotation(melodie, 0, "C", CanonChord.Type.MAJOR, 4); // C-Dur auf Beat 0–3
 *
 * // 2. Validieren (Melodie allein)
 * List<CanonViolation> problems = api.validateMelody(melodie);
 *
 * // 3. Kanon-Konfiguration
 * CanonConfig config = new CanonConfig().setNumVoices(3).setOffsetTicks(CanonNote.WHOLE * 4);
 *
 * // 4. Kanon-Harmonie validieren (mehrere Stimmen)
 * List<CanonViolation> harmony = api.validateCanon(melodie, config);
 *
 * // 5. Kanon als MIDI exportieren
 * api.exportCanon(melodie, config, "kanon.mid");
 * }</pre>
 *
 * @author CanonMaker
 * @version 1.0
 * @see CanonMakerPlugin
 * @see CanonValidator
 */
public interface CanonMakerApi {

    // ==================== MELODIE ERSTELLEN ====================

    /**
     * Erstellt eine leere Melodie mit dem angegebenen Titel.
     *
     * @param title Titel / Bezeichnung der Melodie
     * @return Neue {@link CanonMelody}
     */
    CanonMelody createMelody(String title);

    /**
     * Fügt der Melodie eine Note hinzu.
     *
     * @param melody           Ziel-Melodie
     * @param germanName       Deutscher Notenname (C, Cis, D, ..., H, B)
     * @param scaleMakerOctave Oktavlage (0 = C4, -1 = C3, 1 = C5)
     * @param ticks            Dauer in Ticks (z.B. {@link CanonNote#QUARTER})
     */
    void addNote(CanonMelody melody, String germanName, int scaleMakerOctave, int ticks);

    /**
     * Fügt der Melodie eine Pause hinzu.
     *
     * @param melody Ziel-Melodie
     * @param ticks  Dauer der Pause in Ticks
     */
    void addRest(CanonMelody melody, int ticks);

    /**
     * Annotiert einen Akkord für eine Beat-Position in der Melodie.
     * Wird vom Validator für die Harmonie-Prüfung benötigt.
     *
     * @param melody        Ziel-Melodie
     * @param beatIndex     Beat-Index ab dem dieser Akkord gilt (0-basiert)
     * @param germanRoot    Deutscher Grundton-Name (z.B. "C", "G", "F")
     * @param type          {@link CanonChord.Type#MAJOR} oder {@link CanonChord.Type#MINOR}
     * @param durationBeats Dauer in Beats
     */
    void addChordAnnotation(CanonMelody melody, int beatIndex,
                            String germanRoot, CanonChord.Type type, int durationBeats);

    // ==================== VALIDIERUNG ====================

    /**
     * Prüft die Melodie auf Ambitus, verbotene Intervalle und Rhythmik-Regeln.
     *
     * @param melody Zu prüfende Melodie
     * @return Liste aller Regelverletzungen (leer = fehlerfrei)
     */
    List<CanonViolation> validateMelody(CanonMelody melody);

    /**
     * Prüft die Kanon-Harmonie: Dreiklang auf Schlag 1 und 3,
     * keine kleine Sekundreibung.
     *
     * @param melody Grundmelodie
     * @param config Kanon-Konfiguration
     * @return Liste aller Harmonie-Verletzungen
     */
    List<CanonViolation> validateCanon(CanonMelody melody, CanonConfig config);

    // ==================== EXPORT ====================

    /**
     * Baut den Kanon und exportiert ihn als MIDI-Typ-1-Datei.
     *
     * <p>Jede Stimme erhält einen eigenen MIDI-Track mit Anlauf-Pausen und der
     * Melodie, wiederholt entsprechend {@link CanonConfig#getRepetitions()}.</p>
     *
     * @param melody   Grundmelodie
     * @param config   Kanon-Konfiguration (Stimmen, Instrumente, Tempo, …)
     * @param filePath Ausgabepfad der MIDI-Datei (z.B. {@code "kanon.mid"})
     * @throws Exception wenn das midiMaker-Plugin nicht gefunden wird oder
     *                   die Datei nicht geschrieben werden kann
     */
    void exportCanon(CanonMelody melody, CanonConfig config, String filePath) throws Exception;

    // ==================== VERSION ====================

    /** @return Versionsnummer des Plugins */
    String getVersion();
}
