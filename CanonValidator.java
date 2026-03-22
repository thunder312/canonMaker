import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validiert eine Kanon-Melodie gegen die Kompositions-Regeln.
 *
 * <h2>Geprüfte Regeln (Melodie-Ebene):</h2>
 * <ul>
 *   <li><b>Ambitus:</b> Jede Note muss im Stimmumfang g–e'' (MIDI 55–76) liegen.</li>
 *   <li><b>Intervalle:</b> Aufeinanderfolgende Noten dürfen keinen Tritonus
 *       (6 HT) und keine Septimen (10 oder 11 HT) bilden.</li>
 *   <li><b>Rhythmik:</b> Maximal 2 aufeinanderfolgende Sechzehntelnoten.</li>
 * </ul>
 *
 * <h2>Geprüfte Regeln (Kanon-Ebene, mehrere Stimmen):</h2>
 * <ul>
 *   <li><b>Schlag 1 und 3:</b> Die gleichzeitig klingenden Stimmen müssen einen
 *       vollständigen Dur- oder Moll-Dreiklang bilden (sobald alle Stimmen eingesetzt haben).</li>
 *   <li><b>Schlag 1–4:</b> Keine kleine Sekundreibung (1 Halbton Abstand).</li>
 * </ul>
 *
 * @author CanonMaker
 * @version 1.0
 */
public final class CanonValidator {

    private static final int TICKS_PER_BEAT   = CanonNote.QUARTER;     // 480
    private static final int BEATS_PER_MEASURE = 4;

    private CanonValidator() {}

    // ==================== MELODIE-VALIDIERUNG ====================

    /**
     * Prüft die Melodie allein (ohne Kanon-Kontext) auf Ambitus, Intervalle und Rhythmik.
     *
     * @param melody Zu prüfende Melodie
     * @return Liste aller Regelverletzungen (leer = fehlerfrei)
     */
    public static List<CanonViolation> validateMelody(CanonMelody melody) {
        List<CanonViolation> violations = new ArrayList<>();
        List<CanonNote> notes = melody.getNotes();

        int cursor = 0;
        int consecutiveSixteenths = 0;
        CanonNote prevNote = null;

        for (CanonNote note : notes) {

            if (!note.isRest()) {
                // --- Ambitus ---
                if (note.getMidiPitch() < CanonNote.VOCAL_MIN_PITCH
                        || note.getMidiPitch() > CanonNote.VOCAL_MAX_PITCH) {
                    violations.add(new CanonViolation(
                        CanonViolation.Rule.VOCAL_RANGE, cursor,
                        "Note " + note.getGermanName() + "(MIDI " + note.getMidiPitch()
                        + ") liegt außerhalb des Ambitus MIDI "
                        + CanonNote.VOCAL_MIN_PITCH + "–" + CanonNote.VOCAL_MAX_PITCH
                        + " (g–e'')"));
                }

                // --- Melodische Intervalle zur vorherigen Note ---
                if (prevNote != null && !prevNote.isRest()) {
                    int interval = Math.abs(note.getMidiPitch() - prevNote.getMidiPitch()) % 12;
                    if (interval > 6) interval = 12 - interval; // auf kleinstes Intervall reduzieren
                    if (interval == 6) {
                        violations.add(new CanonViolation(
                            CanonViolation.Rule.FORBIDDEN_INTERVAL, cursor,
                            "Tritonus (verm. Quinte, 6 HT) von "
                            + prevNote.getGermanName() + " nach " + note.getGermanName()));
                    } else {
                        int absInterval = Math.abs(note.getMidiPitch() - prevNote.getMidiPitch());
                        if (absInterval >= 10) {
                            violations.add(new CanonViolation(
                                CanonViolation.Rule.FORBIDDEN_INTERVAL, cursor,
                                "Verbotenes Intervall (" + absInterval + " HT = Septime) von "
                                + prevNote.getGermanName() + " nach " + note.getGermanName()));
                        }
                    }
                }
            }

            // --- Sechzehntel-Regel ---
            if (note.getTicks() == CanonNote.SIXTEENTH) {
                consecutiveSixteenths++;
                if (consecutiveSixteenths > 2) {
                    violations.add(new CanonViolation(
                        CanonViolation.Rule.TOO_MANY_SIXTEENTHS, cursor,
                        "Mehr als 2 aufeinanderfolgende Sechzehntelnoten bei Tick " + cursor));
                }
            } else {
                consecutiveSixteenths = 0;
            }

            if (!note.isRest()) prevNote = note;
            cursor += note.getTicks();
        }

        return violations;
    }

    // ==================== KANON-VALIDIERUNG ====================

    /**
     * Prüft die Harmonie im fertigen Kanon: Dreiklang auf Schlag 1 und 3,
     * keine kleine Sekundreibung auf allen Schlägen.
     *
     * <p>Die Prüfung beginnt ab dem Takt, in dem alle Stimmen eingesetzt haben.</p>
     *
     * @param melody     Grundmelodie
     * @param config     Kanon-Konfiguration
     * @return Liste aller Harmonie-Verletzungen
     */
    public static List<CanonViolation> validateCanon(CanonMelody melody, CanonConfig config) {
        List<CanonViolation> violations = new ArrayList<>();

        int numVoices   = config.getNumVoices();
        int offsetTicks = config.getOffsetTicks();
        int melodyTicks = melody.getTotalTicks();

        if (melodyTicks == 0) return violations;

        // Erster Tick, bei dem alle Stimmen spielen
        int allActiveStart = (numVoices - 1) * offsetTicks;

        // Wir prüfen 1 vollständigen Melodie-Durchlauf im Überlappungsbereich
        int checkEnd = allActiveStart + melodyTicks;

        for (int t = allActiveStart; t < checkEnd; t += TICKS_PER_BEAT) {

            // Beat-Position innerhalb des 4/4-Takts (0–3)
            int beatInMeasure = (t / TICKS_PER_BEAT) % BEATS_PER_MEASURE;

            // MIDI-Pitches aller aktiven Stimmen sammeln
            List<Integer> pitches = collectPitches(melody, numVoices, offsetTicks, melodyTicks, t);
            if (pitches.isEmpty()) continue;

            // --- Kleine Sekundreibung auf allen Schlägen prüfen ---
            checkMinorSecond(pitches, t, violations);

            // --- Dreiklang auf Schlag 1 und 3 (beatInMeasure 0 und 2) ---
            if (beatInMeasure == 0 || beatInMeasure == 2) {
                if (!isCompleteTriad(pitches)) {
                    violations.add(new CanonViolation(
                        CanonViolation.Rule.INCOMPLETE_TRIAD_ON_STRONG_BEAT, t,
                        "Schlag " + (beatInMeasure + 1)
                        + ": kein vollständiger Dreiklang – Töne: " + pitchesToString(pitches)));
                }
            }
        }

        return violations;
    }

    // ==================== HILFSMETHODEN ====================

    /**
     * Sammelt die MIDI-Pitches aller Stimmen zum Tick-Zeitpunkt {@code t}.
     */
    private static List<Integer> collectPitches(CanonMelody melody, int numVoices,
                                                 int offsetTicks, int melodyTicks, int t) {
        List<Integer> pitches = new ArrayList<>();
        for (int v = 0; v < numVoices; v++) {
            int voiceStartTick = v * offsetTicks;
            if (t < voiceStartTick) continue; // Stimme hat noch nicht eingesetzt
            int withinMelody = (t - voiceStartTick) % melodyTicks;
            CanonNote note = melody.getNoteAtTick(withinMelody);
            if (note != null && !note.isRest()) {
                pitches.add(note.getMidiPitch());
            }
        }
        return pitches;
    }

    /**
     * Prüft alle Paare auf kleine Sekundreibung (1 Halbton Abstand).
     */
    private static void checkMinorSecond(List<Integer> pitches, int tick,
                                          List<CanonViolation> violations) {
        for (int i = 0; i < pitches.size() - 1; i++) {
            for (int j = i + 1; j < pitches.size(); j++) {
                int interval = Math.abs(pitches.get(i) - pitches.get(j)) % 12;
                if (interval == 1 || interval == 11) {
                    violations.add(new CanonViolation(
                        CanonViolation.Rule.MINOR_SECOND_DISSONANCE, tick,
                        "Kleine Sekundreibung bei Tick " + tick
                        + ": MIDI " + pitches.get(i) + " und MIDI " + pitches.get(j)));
                }
            }
        }
    }

    /**
     * Prüft ob die gegebenen Pitches (beliebige Anzahl, beliebige Oktaven)
     * einen vollständigen Dur- oder Moll-Dreiklang enthalten.
     *
     * <p>Für jeden Pitch wird geprüft, ob er als Grundton eines Dur- (Grundton + 4 + 7 HT)
     * oder Moll-Dreiklangs (Grundton + 3 + 7 HT) dient, dessen übrige Töne
     * ebenfalls in der Liste vorhanden sind.</p>
     */
    static boolean isCompleteTriad(Collection<Integer> pitches) {
        Set<Integer> pcs = new HashSet<>();
        for (int p : pitches) {
            pcs.add(((p % 12) + 12) % 12);
        }
        for (int root : pcs) {
            // Dur: Grundton + große Terz (4) + Quinte (7)
            if (pcs.contains((root + 4) % 12) && pcs.contains((root + 7) % 12)) return true;
            // Moll: Grundton + kleine Terz (3) + Quinte (7)
            if (pcs.contains((root + 3) % 12) && pcs.contains((root + 7) % 12)) return true;
        }
        return false;
    }

    private static String pitchesToString(List<Integer> pitches) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < pitches.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(pitches.get(i));
        }
        return sb.append("]").toString();
    }
}
