package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used only from the MainReactor thread, so decisions are serialized and need no locks.
 */
public class CommandGate {

    private static final long MANUAL_HOLD_MS = 30_000;
    private static final Set<String> CLEAR_TYPES = Set.of("RESOLVE_LEAK", "EMERGENCY_CLEAR");
    // data reports, not commands: never held or locked
    private static final Set<String> DATA_TYPES = Set.of(
            "UPDATE_LINE_LOAD", "UPDATE_DEVICE_LOAD", "POWER_LINE_SHEDDED", "LOAD_RECHECK");
    private final Map<String, Long> shedLockedUntil = new HashMap<>();
    private final Map<String, Long> manualHoldUntil = new HashMap<>();
    private final Set<String> emergencyLocked = new HashSet<>();
    private static final Set<String> NOTIFICATION_TYPES = Set.of(
            "TRIGGER_LEAK", "DARKNESS_DETECTED", "SENSOR_TRIGGER",
            "WATER_VALVE_OPENED", "WATER_VALVE_CLOSED", "LEAK_DETECTED",
            "UPDATE_LINE_LOAD", "UPDATE_DEVICE_LOAD", "POWER_LINE_SHEDDED", "LOAD_RECHECK");

    public void shedLock(String compID, long until) {
        shedLockedUntil.put(compID, until);
    }

    public void lock(String compID) {
        emergencyLocked.add(compID);
    }

    /**
     * Returns null if the event is allowed, otherwise the reason it is blocked.
     */
    public String blockReason(Event e, long now) {
        String type = e.getType();
        if (Protocol.isTelemetry(type) || NOTIFICATION_TYPES.contains(type)) return null;

        if (CLEAR_TYPES.contains(type)) {
            emergencyLocked.clear();
            return null;
        }

        Priority p = e.getPriority();
        String id = e.getCompId();

        if (p == Priority.EMERGENCY) return null;

        if (emergencyLocked.contains(id)) {
            return "emergency lock on " + id + " (released by Resolve Leak)";
        }

        if (p == Priority.USER_OVERRIDE) {
            manualHoldUntil.put(id, now + MANUAL_HOLD_MS);
            return null;
        }

        long hold = manualHoldUntil.getOrDefault(id, 0L);
        if (hold > now) return "manual hold on " + id + " for another " + (hold - now) / 1000 + " s";

        long shed = shedLockedUntil.getOrDefault(id, 0L);
        if (shed > now) return "shed lock on " + id + " for another " + (shed - now) / 1000 + " s";

        return null;
    }

    public boolean allow(Event e, long now) {
        return blockReason(e, now) == null;
    }
}