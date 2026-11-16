package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Event;
import java.util.HashMap;
import java.util.Map;

public class PowerBreakerActuator {
    private final String componentId;
    private boolean mainBreakerOn = true;

    private final Map<String, Boolean> lines = new HashMap<>();
    private final Map<String, Double> lineLoadsKw = new HashMap<>();
    private final Map<String, Map<String, Double>> deviceLoadsKw = new HashMap<>();

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
            case "TOGGLE_MAIN" -> mainBreakerOn = Boolean.parseBoolean(payload);
            case "TOGGLE_LINE" -> {
                String[] parts = payload.split(":");
                if (parts.length == 2 && lines.containsKey(parts[0])) {
                    lines.put(parts[0], Boolean.parseBoolean(parts[1]));
                }
            }
            case "UPDATE_LINE_LOAD" -> {
                String[] parts = payload.split(":");
                if (parts.length == 2 && lines.containsKey(parts[0])) {
                    lineLoadsKw.put(parts[0], Math.max(0.0, Double.parseDouble(parts[1])));
                }
            }
            case "UPDATE_DEVICE_LOAD" -> {
                String[] parts = payload.split(":");
                if (parts.length == 3 && lines.containsKey(parts[0])) {
                    deviceLoadsKw.computeIfAbsent(parts[0], k -> new HashMap<>())
                            .put(parts[1], Math.max(0.0, Double.parseDouble(parts[2])));
                }
            }
        }
    }

    public synchronized double getDeviceLoadKw(String line, String deviceId) {
        return deviceLoadsKw.getOrDefault(line, Map.of()).getOrDefault(deviceId, 0.0);
    }

    public synchronized void removeDeviceLoad(String line, String deviceId) {
        Map<String, Double> lineDevices = deviceLoadsKw.get(line);
        if (lineDevices != null) {
            lineDevices.remove(deviceId);
        }
    }

    private double lineLoad(String line) {
        double devices = deviceLoadsKw.getOrDefault(line, Map.of()).values()
                .stream().mapToDouble(Double::doubleValue).sum();
        return lineLoadsKw.getOrDefault(line, 0.0) + devices;
    }

    public synchronized void clearLine(String line) {
        lineLoadsKw.put(line, 0.0);
        deviceLoadsKw.remove(line);
    }

    public synchronized double getTotalPowerLoadKw() {
        if (!mainBreakerOn) return 0.0;
        return lines.entrySet().stream()
                .filter(Map.Entry::getValue)
                .mapToDouble(e -> lineLoad(e.getKey()))
                .sum();
    }

    public synchronized double getRemainingCapacityKw() {
        if (!mainBreakerOn) return 0.0;
        return Math.max(0.0, MAX_CAPACITY_KW - getTotalPowerLoadKw());
    }

    public synchronized boolean isLineActive(String lineName) {
        return lines.getOrDefault(lineName, false);
    }

    public synchronized boolean isLineEnergized(String lineName) {
        return mainBreakerOn && lines.getOrDefault(lineName, false);
    }

    public synchronized boolean isMainBreakerOn() { return mainBreakerOn; }
    public synchronized Map<String, Boolean> getLines() { return new HashMap<>(lines); }

    public synchronized Map<String, Double> getLineLoadsKw() {
        Map<String, Double> result = new HashMap<>();
        lines.keySet().forEach(line -> result.put(line, lineLoad(line)));
        return result;
    }
}