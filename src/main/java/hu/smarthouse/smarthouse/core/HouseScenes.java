package hu.smarthouse.smarthouse.core;

import java.util.List;
import java.util.Map;

public final class HouseScenes {
    private HouseScenes() {}

    public record Step(String compId, String type, String message) {}

    private static Step line(String line, boolean on) {
        return new Step("MAIN_POWER_BREAKER", "TOGGLE_LINE", line + ":" + on);
    }

    private static Step off(String compId) { return new Step(compId, "TURN_OFF", ""); }
    private static Step on(String compId)  { return new Step(compId, "TURN_ON", ""); }
    private static Step boiler(String mode) { return new Step("BATHROOM_BOILER", "SET_BOILER_MODE", mode); }

    // Order matters per component: lines come back on before devices start,
    // and devices are switched off before their line is cut. The door must CLOSE before it can LOCK.
    private static final Map<String, List<Step>> SCENES = Map.of(

            "HOME", List.of(
                    line("KITCHEN_LINE", true),
                    line("BATHROOM_LINE", true),
                    line("LIVING_ROOM_LINE", true),
                    on("LIVING_ROOM_LIGHT"),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_UP", ""),
                    new Step("FRONT_DOOR", "UNLOCK", ""),
                    new Step("LIVING_ROOM_THERMOSTAT", "MODE_HEAT", ""),
                    boiler("ECO")),

            "AWAY", List.of(
                    off("BATHROOM_LIGHT"),
                    off("KITCHEN_LIGHT"),
                    off("LIVING_ROOM_LIGHT"),
                    off("KITCHEN_PLUG"),
                    off("BATHROOM_PLUG"),
                    off("BATHROOM_FAN"),
                    new Step("LIVING_ROOM_WINDOW", "CLOSE", ""),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_DOWN", ""),
                    new Step("FRONT_DOOR", "CLOSE", ""),
                    new Step("FRONT_DOOR", "LOCK", ""),
                    new Step("LIVING_ROOM_THERMOSTAT", "MODE_OFF", ""),
                    boiler("OFF"),
                    line("KITCHEN_LINE", false),
                    line("BATHROOM_LINE", false)),

            "NIGHT", List.of(
                    line("KITCHEN_LINE", true),
                    line("BATHROOM_LINE", true),
                    line("LIVING_ROOM_LINE", true),
                    off("BATHROOM_LIGHT"),
                    off("KITCHEN_LIGHT"),
                    off("LIVING_ROOM_LIGHT"),
                    new Step("LIVING_ROOM_WINDOW", "CLOSE", ""),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_DOWN", ""),
                    new Step("FRONT_DOOR", "CLOSE", ""),
                    new Step("FRONT_DOOR", "LOCK", ""),
                    boiler("OFF")),

            "ECO", List.of(
                    off("KITCHEN_PLUG"),
                    off("BATHROOM_PLUG"),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_UP", ""),
                    new Step("LIVING_ROOM_THERMOSTAT", "MODE_OFF", ""),
                    boiler("ECO"))
    );

    public static List<Step> get(String name) { return SCENES.get(name); }
}