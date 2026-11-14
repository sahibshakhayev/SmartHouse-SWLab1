package hu.smarthouse.smarthouse.core;

import java.util.List;
import java.util.function.Predicate;

public final class ShedPlanner {
    private ShedPlanner() {}

    public static final long SHED_LOCK_MS = 60_000;

    public static final List<List<String>> TIERS = List.of(
            List.of("KITCHEN_PLUG", "BATHROOM_PLUG", "LIVING_ROOM_TV", "LIVING_ROOM_MUSIC"), // 1: non-essential
            List.of("BATHROOM_BOILER", "LIVING_ROOM_THERMOSTAT"),                            // 2: boiler / HVAC
            List.of("LIVING_ROOM_LINE", "KITCHEN_LINE", "BATHROOM_LINE"));                   // 3: room feeders, one at a time

    public static boolean isFeeder(String id) { return id.endsWith("_LINE"); }

    /** The next group to shed, or empty if nothing is left. */
    public static List<String> nextStep(Predicate<String> alreadyShed, Predicate<String> lineActive) {
        for (int i = 0; i < TIERS.size(); i++) {
            boolean feeders = (i == TIERS.size() - 1);
            List<String> remaining = TIERS.get(i).stream()
                    .filter(id -> !alreadyShed.test(id))
                    .filter(id -> !feeders || lineActive.test(id))
                    .toList();
            if (!remaining.isEmpty()) return feeders ? List.of(remaining.get(0)) : remaining;
        }
        return List.of();
    }
}