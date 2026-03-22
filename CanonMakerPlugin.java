/**
 * Plugin-Interface für canonMaker.
 *
 * <p>Dieses Interface definiert den Vertrag zwischen dem Hauptprogramm und dem
 * canonMaker-Plugin. Das Hauptprogramm lädt Implementierungen über den Java
 * {@link java.util.ServiceLoader}-Mechanismus.</p>
 *
 * <h2>Verwendung im Hauptprogramm:</h2>
 * <pre>{@code
 * import java.util.ServiceLoader;
 *
 * ServiceLoader<CanonMakerPlugin> loader = ServiceLoader.load(CanonMakerPlugin.class);
 * CanonMakerPlugin plugin = loader.findFirst()
 *     .orElseThrow(() -> new RuntimeException("canonMaker-Plugin nicht gefunden"));
 *
 * plugin.initialize();
 * CanonMakerApi api = plugin.getApi();
 *
 * CanonMelody melodie = api.createMelody("Mein Kanon");
 * // ... Noten hinzufügen ...
 * api.exportCanon(melodie, new CanonConfig(), "kanon.mid");
 *
 * plugin.shutdown();
 * }</pre>
 *
 * @author CanonMaker
 * @version 1.0
 * @see CanonMakerApi
 * @see java.util.ServiceLoader
 */
public interface CanonMakerPlugin {

    /** @return Plugin-Name (z.B. "CanonMaker") */
    String getName();

    /** @return Versionsnummer (z.B. "1.0.0") */
    String getVersion();

    /** @return Kurze Beschreibung des Plugins */
    String getDescription();

    /** Initialisiert das Plugin. Muss vor {@link #getApi()} aufgerufen werden. */
    void initialize();

    /** Fährt das Plugin herunter und gibt Ressourcen frei. */
    void shutdown();

    /**
     * Gibt die API-Instanz zurück.
     *
     * @return Die {@link CanonMakerApi}-Instanz
     * @throws IllegalStateException wenn {@link #initialize()} noch nicht aufgerufen wurde
     */
    CanonMakerApi getApi();
}
