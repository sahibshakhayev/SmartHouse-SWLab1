package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.Set;

public final class PriorityMatrix {
    private PriorityMatrix() {}

    private static final Set<String> EMERGENCY_TYPES = Set.of(
            "LEAK_DETECTED", "THERMAL_LIMIT_TRIP", "WATER_VALVE_CLOSED");

    public static Priority resolve(Event e) {
        if (EMERGENCY_TYPES.contains(e.getType())) return Priority.EMERGENCY;
        if (e.getPriority() != null) return e.getPriority();
        return Protocol.isTelemetry(e.getType()) ? Priority.BACKGROUND : Priority.AUTOMATION;
    }
}