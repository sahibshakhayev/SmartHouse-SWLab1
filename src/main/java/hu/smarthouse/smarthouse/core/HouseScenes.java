package hu.smarthouse.smarthouse.core;

import java.util.List;
import java.util.Map;

public final class HouseScenes {
    private HouseScenes() {}

    public record Step(String compId, String type, String message) {}

    private static Step line(String line, boolean on) { return new Step("MAIN_POWER_BREAKER", "TOGGLE_LINE", line + ":" + on); }
    private static Step off(String compId) { return new Step(compId, "TURN_OFF", ""); }
    private static Step on(String compId)  { return new Step(compId, "TURN_ON", ""); }
    private static Step boiler(String mode) { return new Step("BATHROOM_BOILER", "SET_BOILER_MODE", mode); }
    private static Step tv(String type, String msg) { return new Step("LIVING_ROOM_TV", type, msg); }
    private static Step music(String type, String msg) { return new Step("LIVING_ROOM_MUSIC", type, msg); }
    private static Step pause(int ms) { return new Step("", "PAUSE", String.valueOf(ms)); }

    private static final Map<String, List<Step>> SCENES = Map.of(

            "HOME", List.of(
                    line("KITCHEN_LINE", true),
                    line("BATHROOM_LINE", true),
                    line("LIVING_ROOM_LINE", true),
                    pause(400),
                    on("LIVING_ROOM_LIGHT"),
                    tv("TURN_ON", ""),
                    tv("CHANNEL_SET", "1"),
                    tv("VOLUME_SET", "20"),
                    music("TURN_OFF", ""),
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
                    tv("TURN_OFF", ""),
                    music("TURN_OFF", ""),
                    new Step("LIVING_ROOM_WINDOW", "CLOSE", ""),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_DOWN", ""),
                    new Step("FRONT_DOOR", "CLOSE", ""),
                    new Step("FRONT_DOOR", "LOCK", ""),
                    new Step("LIVING_ROOM_THERMOSTAT", "MODE_OFF", ""),
                    boiler("OFF"),
                    pause(300),
                    line("KITCHEN_LINE", false),
                    line("BATHROOM_LINE", false)),

            "NIGHT", List.of(
                    line("KITCHEN_LINE", true),
                    line("BATHROOM_LINE", true),
                    line("LIVING_ROOM_LINE", true),
                    pause(400),
                    off("BATHROOM_LIGHT"),
                    off("KITCHEN_LIGHT"),
                    off("LIVING_ROOM_LIGHT"),
                    tv("TURN_OFF", ""),
                    music("TURN_ON", ""),
                    music("TRACK_SET", "4"),      // Sleep Waves
                    music("VOLUME_SET", "10"),
                    new Step("LIVING_ROOM_WINDOW", "CLOSE", ""),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_DOWN", ""),
                    new Step("FRONT_DOOR", "CLOSE", ""),
                    new Step("FRONT_DOOR", "LOCK", ""),
                    boiler("OFF")),

            "ECO", List.of(
                    off("KITCHEN_PLUG"),
                    off("BATHROOM_PLUG"),
                    tv("TURN_OFF", ""),
                    music("TURN_OFF", ""),
                    new Step("LIVING_ROOM_WINDOW", "SHADE_UP", ""),
                    new Step("LIVING_ROOM_THERMOSTAT", "MODE_OFF", ""),
                    boiler("ECO"))
    );

    public static List<Step> get(String name) { return SCENES.get(name); }
}