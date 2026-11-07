package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Event;
import java.util.HashMap;
import java.util.Map;

public class PowerBreakerActuator {
    private final String componentId;
    private boolean mainBreakerOn = true;

    private final Map<String, Boolean> lines = new HashMap<>();
    private final Map<String, Double> lineLoadsKw = new HashMap<>();

    public static final double MAX_CAPACITY_KW = 5.0;

    public PowerBreakerActuator(String componentId) {
        this.componentId = componentId;
        lines.put("KITCHEN_LINE", true);
        lines.put("BATHROOM_LINE", true);
        lines.put("LIVING_ROOM_LINE", true);

        lineLoadsKw.put("KITCHEN_LINE", 0.0);
        lineLoadsKw.put("BATHROOM_LINE", 0.0);
        lineLoadsKw.put("LIVING_ROOM_LINE", 0.0);
    }

    public synchronized void execute(Event event) {
        String type = event.getType();
        String payload = event.getMessage();

        switch (type) {
            case "TOGGLE_MAIN" -> this.mainBreakerOn = Boolean.parseBoolean(payload);
            case "TOGGLE_LINE" -> {
                String[] parts = payload.split(":");
                if (parts.length == 2 && lines.containsKey(parts[0])) {
                    lines.put(parts[0], Boolean.parseBoolean(parts[1]));
                }
            }
            case "UPDATE_LINE_LOAD" -> {
                String[] parts = payload.split(":");
                if (parts.length == 2 && lineLoadsKw.containsKey(parts[0])) {
                    lineLoadsKw.put(parts[0], Math.max(0.0, Double.parseDouble(parts[1])));
                }
            }
        }
    }

    public synchronized double getTotalPowerLoadKw() {
        if (!mainBreakerOn) return 0.0;
        return lines.entrySet().stream()
                .filter(Map.Entry::getValue)
                .mapToDouble(entry -> lineLoadsKw.getOrDefault(entry.getKey(), 0.0))
                .sum();
    }

    public synchronized boolean isLineActive(String lineName) {
        return mainBreakerOn && lines.getOrDefault(lineName, false);
    }

    public synchronized boolean isMainBreakerOn() { return mainBreakerOn; }
    public synchronized Map<String, Boolean> getLines() { return new HashMap<>(lines); }
    public synchronized Map<String, Double> getLineLoadsKw() { return new HashMap<>(lineLoadsKw); }
}