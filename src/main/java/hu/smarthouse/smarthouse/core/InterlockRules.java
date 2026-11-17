package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.List;
import java.util.Map;

public final class InterlockRules {
    private InterlockRules() {}

    private record Action(String type, String message, String compId) {}

    private static final Map<String, List<Action>> RULES = Map.of(
            "LEAK_DETECTED", List.of(
                    new Action("VALVE_CLOSE", "Leak interlock", "HOUSE_WATER_PIPES"),
                    new Action("TOGGLE_LINE", "BATHROOM_LINE:false", "MAIN_POWER_BREAKER"),
                    new Action("TOGGLE_LINE", "KITCHEN_LINE:false", "MAIN_POWER_BREAKER"),
                    new Action("TURN_OFF", "Leak interlock", "BATHROOM_BOILER")),
            "THERMAL_LIMIT_TRIP", List.of(
                    new Action("TURN_OFF", "Thermal limit trip", "BATHROOM_BOILER"))
            // no rule for WATER_VALVE_CLOSED: BoilerReactor handles the dry-run cutoff itself
    );

    public static List<Event> consequencesOf(Event trigger) {
        return RULES.getOrDefault(trigger.getType(), List.of()).stream()
                .map(a -> new Event(a.type(), a.message(), a.compId(), Priority.EMERGENCY))
                .toList();
    }
}