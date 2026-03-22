/**
 * ServiceLoader-Implementierung des {@link CanonMakerPlugin}-Interfaces.
 *
 * <p>Wird vom Java {@link java.util.ServiceLoader} automatisch entdeckt,
 * wenn {@code canonmaker.jar} im Classpath des Hauptprogramms liegt.
 * Die Registrierung erfolgt über {@code META-INF/services/CanonMakerPlugin}.</p>
 *
 * @author CanonMaker
 * @version 1.0
 */
public class CanonMakerPluginImpl implements CanonMakerPlugin {

    private CanonMakerApi api;

    /** Öffentlicher No-Arg-Konstruktor, erforderlich für den ServiceLoader. */
    public CanonMakerPluginImpl() {}

    @Override
    public String getName() {
        return "CanonMaker";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Kanon-Generator auf Basis von midiMaker – prüft Dreiklänge, Ambitus und Intervalle";
    }

    @Override
    public void initialize() {
        api = new CanonMakerApiImpl();
    }

    @Override
    public void shutdown() {
        api = null;
    }

    @Override
    public CanonMakerApi getApi() {
        if (api == null) {
            throw new IllegalStateException(
                "CanonMaker-Plugin ist nicht initialisiert. initialize() zuerst aufrufen.");
        }
        return api;
    }

    @Override
    public String toString() {
        return getName() + " v" + getVersion();
    }
}
