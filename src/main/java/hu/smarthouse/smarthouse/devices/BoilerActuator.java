package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Event;

public class BoilerActuator {
    private final String componentId;
    private String mode = "OFF"; // "OFF", "ECO", "BOOST"
    private double currentWaterTemp = 20.0;
    private boolean waterSupplyAvailable = true;

    public BoilerActuator(String componentId) {
        this.componentId = componentId;
    }

    public synchronized void execute(Event event) {
        String type = event.getType();
        String payload = event.getMessage();

        switch (type) {
            case "SET_BOILER_MODE" -> {
                if (!waterSupplyAvailable && !"OFF".equalsIgnoreCase(payload)) {
                    throw new IllegalStateException("Dry-Run Protection Active: Cannot turn on boiler without water supply!");
                }
                setModeInternal(payload.toUpperCase());
            }
            case "WATER_VALVE_CLOSED" -> {
                this.waterSupplyAvailable = false;
                this.mode = "OFF";
            }
            case "WATER_VALVE_OPENED" -> this.waterSupplyAvailable = true;
            case "TURN_OFF" -> this.mode = "OFF";
        }
    }

    private void setModeInternal(String newMode) {
        if ("OFF".equals(newMode) || "ECO".equals(newMode) || "BOOST".equals(newMode)) {
            this.mode = newMode;
        }
    }

    public synchronized boolean simulateTemperatureStep() {
        double target = getTargetTemp();
        if (Math.abs(currentWaterTemp - target) < 0.1) return false;

        if (currentWaterTemp < target) {
            // В режиме BOOST греем быстрее
            double heatRate = "BOOST".equals(mode) ? 0.8 : 0.4;
            currentWaterTemp = Math.min(target, currentWaterTemp + heatRate);
        } else {
            // Медленное остывание
            currentWaterTemp = Math.max(target, currentWaterTemp - 0.2);
        }
        return true;
    }

    public synchronized double getTargetTemp() {
        return switch (mode) {
            case "ECO" -> 40.0;
            case "BOOST" -> 65.0;
            default -> 20.0; // OFF
        };
    }

    public synchronized double getPowerDrawKw() {
        return switch (mode) {
            case "ECO" -> 1.0;
            case "BOOST" -> 2.2;
            default -> 0.0;
        };
    }

    public synchronized String getMode() { return mode; }
    public synchronized double getCurrentWaterTemp() { return currentWaterTemp; }
    public synchronized boolean isWaterSupplyAvailable() { return waterSupplyAvailable; }
}