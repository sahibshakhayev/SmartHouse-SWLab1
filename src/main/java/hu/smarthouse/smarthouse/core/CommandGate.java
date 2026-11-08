package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Used only from the MainReactor thread, so decisions are serialized and need no locks. */
public class CommandGate {

    private static final long MANUAL_HOLD_MS = 30_000;
    private static final Set<String> CLEAR_TYPES = Set.of("RESOLVE_LEAK", "EMERGENCY_CLEAR");
    // data reports, not commands: never held or locked
    private static final Set<String> DATA_TYPES = Set.of("UPDATE_LINE_LOAD", "POWER_LINE_SHEDDED");

    private final Map<String, Long> manualHoldUntil = new HashMap<>();
    private final Set<String> emergencyLocked = new HashSet<>();

    public void lock(String compID) { emergencyLocked.add(compID); }

    public boolean allow(Event e, long now) {
        String type = e.getType();
        if (Protocol.isTelemetry(type) || DATA_TYPES.contains(type)) return true;

        if (CLEAR_TYPES.contains(type)) {
            emergencyLocked.clear();
            return true;
        }

        Priority p = e.getPriority();
        String id = e.getCompId();

        if (p == Priority.EMERGENCY) return true;
        if (emergencyLocked.contains(id)) return false;

        if (p == Priority.USER_OVERRIDE) {
            manualHoldUntil.put(id, now + MANUAL_HOLD_MS);
            return true;
        }

        Long until = manualHoldUntil.get(id);   // AUTOMATION / BACKGROUND
        return until == null || now >= until;
    }
}