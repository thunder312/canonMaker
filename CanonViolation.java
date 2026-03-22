/**
 * Beschreibt eine Regelverletzung bei der Validierung einer Kanon-Melodie.
 *
 * <p>Wird von {@link CanonValidator} erzeugt und über die
 * {@link CanonMakerApi}-Methoden zurückgegeben.</p>
 *
 * @author CanonMaker
 * @version 1.0
 * @see CanonValidator
 */
public final class CanonViolation {

    /**
     * Art der Regelverletzung.
     */
    public enum Rule {

        /**
         * Note liegt außerhalb des Singstimmen-Ambitus (g–e'', MIDI 55–76).
         */
        VOCAL_RANGE,

        /**
         * Verbotenes melodisches Intervall zwischen zwei aufeinanderfolgenden Noten:
         * Septimen (10 oder 11 Halbtöne) oder verminderte Quinte (6 Halbtöne = Tritonus).
         */
        FORBIDDEN_INTERVAL,

        /**
         * Mehr als 2 aufeinanderfolgende Sechzehntelnoten (Rhythmus-Regel).
         */
        TOO_MANY_SIXTEENTHS,

        /**
         * Kleine Sekundreibung (1 Halbton Abstand) zwischen gleichzeitig erklingenden
         * Stimmen auf Schlägen 1, 2, 3 oder 4.
         */
        MINOR_SECOND_DISSONANCE,

        /**
         * Auf Schlag 1 oder 3 entsteht kein vollständiger Dur- oder Moll-Dreiklang
         * aus den gleichzeitig erklingenden Stimmen.
         */
        INCOMPLETE_TRIAD_ON_STRONG_BEAT
    }

    private final Rule   rule;
    private final int    tickPosition;
    private final String description;

    /**
     * Erstellt eine Regelverletzung.
     *
     * @param rule         Die verletzte Regel
     * @param tickPosition Tick-Position innerhalb der Melodie (oder des Kanons)
     * @param description  Lesbare Beschreibung
     */
    public CanonViolation(Rule rule, int tickPosition, String description) {
        this.rule         = rule;
        this.tickPosition = tickPosition;
        this.description  = description;
    }

    /** @return Die verletzte Regel */
    public Rule getRule() { return rule; }

    /** @return Tick-Position des Problems */
    public int getTickPosition() { return tickPosition; }

    /** @return Beschreibung des Problems */
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + rule + " @ tick " + tickPosition + "] " + description;
    }
}
