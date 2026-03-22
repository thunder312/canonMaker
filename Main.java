import java.util.List;
import java.util.ServiceLoader;

/**
 * Demo-Anwendung für das canonMaker-Plugin.
 *
 * <p>Demonstriert den vollständigen Workflow: Plugin laden, eine 3-taktige
 * Kanon-Melodie in C-Dur anlegen, Regeln validieren und als MIDI-Datei exportieren.</p>
 *
 * <h2>Melodie-Design (3 Takte, 4/4, 12 Beats):</h2>
 * <pre>
 *   Takt 1 (C-Dur, T):   C4 | D4 | G4 | F4
 *   Takt 2 (G-Dur, D):   E4 | A4 | H4 | A4
 *   Takt 3 (C-Dur, T):   G4 | F4 | D4 | C4
 * </pre>
 *
 * <h2>Kanonische Harmonie (3 Stimmen, Versatz 1 Takt = 4 Beats):</h2>
 * <p>Bei 3 Stimmen mit Versatz 4 Beats und 12-Beat-Melodie spielen die Stimmen
 * an jedem Schlag unterschiedliche Melodie-Positionen (beat b, b-4, b-8):
 * <ul>
 *   <li>Schlag 1 (Beat 0): C4 + G4 + E4 = C-Dur ✓</li>
 *   <li>Schlag 3 (Beat 2): G4 + H4 + D4 = G-Dur ✓</li>
 * </ul>
 *
 * <h2>Kompilieren und starten:</h2>
 * <pre>
 *   build.bat
 *   java -cp "bin;..\midiMaker\bin\midimaker.jar" Main
 * </pre>
 *
 * @author CanonMaker
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== canonMaker Demo ===");
        System.out.println();

        // ---------------------------------------------------------------
        // 1. Plugin laden
        // ---------------------------------------------------------------
        ServiceLoader<CanonMakerPlugin> loader = ServiceLoader.load(CanonMakerPlugin.class);
        CanonMakerPlugin plugin = loader.findFirst()
            .orElseThrow(() -> new RuntimeException("CanonMaker-Plugin nicht gefunden!"));

        plugin.initialize();
        System.out.println("Plugin:      " + plugin);
        System.out.println("Beschreibung: " + plugin.getDescription());
        System.out.println();

        CanonMakerApi api = plugin.getApi();

        // ---------------------------------------------------------------
        // 2. Melodie anlegen (3 Takte, 4/4, je 4 Viertelnoten)
        //
        //    Konstruiert so, dass bei 3 Stimmen mit Versatz 1 Takt (4 Beats)
        //    auf den Schlägen 1 und 3 vollständige Dreiklänge entstehen:
        //
        //    Beat  0: C4  | Beat  4: E4  | Beat  8: G4  → C-Dur auf Schlag 1
        //    Beat  2: G4  | Beat  6: H4  | Beat 10: D4  → G-Dur auf Schlag 3
        // ---------------------------------------------------------------
        CanonMelody melodie = api.createMelody("canonMaker Demo – C-Dur-Kanon");

        // Takt 1 (Beat 0–3)
        api.addNote(melodie, "C",  0, CanonNote.QUARTER);   // Beat 0 – C4
        api.addNote(melodie, "D",  0, CanonNote.QUARTER);   // Beat 1 – D4
        api.addNote(melodie, "G",  0, CanonNote.QUARTER);   // Beat 2 – G4
        api.addNote(melodie, "F",  0, CanonNote.QUARTER);   // Beat 3 – F4

        // Takt 2 (Beat 4–7)
        api.addNote(melodie, "E",  0, CanonNote.QUARTER);   // Beat 4 – E4
        api.addNote(melodie, "A",  0, CanonNote.QUARTER);   // Beat 5 – A4
        api.addNote(melodie, "H",  0, CanonNote.QUARTER);   // Beat 6 – H4 (= B natural)
        api.addNote(melodie, "A",  0, CanonNote.QUARTER);   // Beat 7 – A4

        // Takt 3 (Beat 8–11)
        api.addNote(melodie, "G",  0, CanonNote.QUARTER);   // Beat 8  – G4
        api.addNote(melodie, "F",  0, CanonNote.QUARTER);   // Beat 9  – F4
        api.addNote(melodie, "D",  0, CanonNote.QUARTER);   // Beat 10 – D4
        api.addNote(melodie, "C",  0, CanonNote.QUARTER);   // Beat 11 – C4

        // Akkord-Annotationen (für Harmonie-Validierung)
        api.addChordAnnotation(melodie, 0, "C",  CanonChord.Type.MAJOR, 4); // Takt 1: C-Dur (T)
        api.addChordAnnotation(melodie, 4, "G",  CanonChord.Type.MAJOR, 4); // Takt 2: G-Dur (D)
        api.addChordAnnotation(melodie, 8, "C",  CanonChord.Type.MAJOR, 4); // Takt 3: C-Dur (T)

        System.out.println("Melodie: " + melodie);
        System.out.println();

        // ---------------------------------------------------------------
        // 3. Melodie validieren (Ambitus, Intervalle, Rhythmik)
        // ---------------------------------------------------------------
        System.out.println("--- Melodie-Validierung ---");
        List<CanonViolation> melodyProblems = api.validateMelody(melodie);
        if (melodyProblems.isEmpty()) {
            System.out.println("  ✓ Keine Regelverletzungen gefunden.");
        } else {
            for (CanonViolation v : melodyProblems) {
                System.out.println("  ✗ " + v);
            }
        }
        System.out.println();

        // ---------------------------------------------------------------
        // 4. Kanon-Konfiguration: 3 Stimmen, Versatz 1 Takt, Tempo 80 BPM
        //    Instrumente: Oboe (68), Klarinette (71), Fagott (70)
        // ---------------------------------------------------------------
        CanonConfig config = new CanonConfig()
            .setNumVoices(3)
            .setOffsetTicks(CanonNote.WHOLE)            // 1 Takt = 4 × 480 = 1920 ticks
            .setInstruments(new int[]{68, 71, 70})     // Oboe, Klarinette, Fagott
            .setChannels(new int[]{0, 1, 2})
            .setOctaveOffsets(new int[]{0, 0, -1})     // Fagott eine Oktave tiefer → Bassschlüssel
            .setTempoBpm(80)
            .setRepetitions(2);

        System.out.println("Konfiguration: " + config);
        System.out.println();

        // ---------------------------------------------------------------
        // 5. Kanon-Harmonie validieren (Dreiklänge auf Schlag 1+3, Sekundreibungen)
        // ---------------------------------------------------------------
        System.out.println("--- Kanon-Harmonie-Validierung ---");
        List<CanonViolation> canonProblems = api.validateCanon(melodie, config);
        if (canonProblems.isEmpty()) {
            System.out.println("  ✓ Kanon erfüllt alle Harmonie-Regeln.");
        } else {
            for (CanonViolation v : canonProblems) {
                System.out.println("  ✗ " + v);
            }
        }
        System.out.println();

        // ---------------------------------------------------------------
        // 6. MIDI-Datei exportieren
        // ---------------------------------------------------------------
        String outputPath = "canon.mid";
        api.exportCanon(melodie, config, outputPath);
        System.out.println("Exportiert nach: " + outputPath);
        System.out.println("  → 3 Stimmen (Oboe, Klarinette, Fagott)");
        System.out.println("  → Versatz je 1 Takt, je 2 Wiederholungen");
        System.out.println("  → Tempo: 80 BPM");
        System.out.println();

        // ---------------------------------------------------------------
        // 7. Kanonische Harmonie erläutern
        // ---------------------------------------------------------------
        System.out.println("--- Kanonische Harmonie (Schlag-Analyse) ---");
        System.out.println("  Schlag 1 (Beat 0): Stimme1=C4 + Stimme2=G4 + Stimme3=E4 → C-Dur ✓");
        System.out.println("  Schlag 2 (Beat 1): Stimme1=D4 + Stimme2=F4 + Stimme3=A4 → D-Moll ✓");
        System.out.println("  Schlag 3 (Beat 2): Stimme1=G4 + Stimme2=H4 + Stimme3=D4 → G-Dur ✓");
        System.out.println("  Schlag 4 (Beat 3): Stimme1=F4 + Stimme2=A4 + Stimme3=C4 → F-Dur ✓");
        System.out.println();

        plugin.shutdown();
        System.out.println("=== Demo abgeschlossen. Datei: " + outputPath + " ===");
    }
}
