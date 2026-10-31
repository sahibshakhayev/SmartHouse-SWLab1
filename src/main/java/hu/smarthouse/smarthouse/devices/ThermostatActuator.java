package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;

public class ThermostatActuator extends Actuator {
    private double currentTemp = 20.0;
    private double targetTemp = 22.0;
    private double outdoorTemp = 15.0; // Стартовое значение до первого ответа API
    private boolean autoMode = true;
    private String mode = "OFF";

    public ThermostatActuator(String compId) {
        super(compId);
    }

    @Override
    protected synchronized void performAction(Event event) {
        System.out.println("Thermostat Actuator: " + event.getMessage());
        switch (event.getType()) {
            case "MODE_HEAT" -> { mode = "HEATING"; autoMode = false; }
            case "MODE_COOL" -> { mode = "COOLING"; autoMode = false; }
            case "MODE_OFF" -> { mode = "OFF"; autoMode = false; }
            case "TOGGLE_AUTO" -> autoMode = !autoMode;
            case "TEMP_UP" -> {
                if (targetTemp < 30.0) targetTemp += 1.0;
                else System.out.println("[Thermostat] Max limit reached (30°C)");
            }
            case "TEMP_DOWN" -> {
                if (targetTemp > 15.0) targetTemp -= 1.0;
                else System.out.println("[Thermostat] Min limit reached (15°C)");
            }
            case "OUTDOOR_TEMP_UPDATE" -> outdoorTemp = Double.parseDouble(event.getMessage());
            case "INTERNAL_SET_MODE" -> mode = event.getMessage();
            default -> throw new IllegalArgumentException(
                    "Thermostat does not support event type: " + event.getType());
        }
    }

    public synchronized boolean simulateAmbientFluctuation() {
        double oldTemp = currentTemp;
        String oldMode = this.mode;


        double thermalPull = (outdoorTemp - currentTemp) * 0.05;

        if ("HEATING".equals(mode)) {
            if (currentTemp < 30.0) {
                currentTemp += 0.5;
            } else {
                System.out.println("[Thermostat WARNING] Max limit 30°C reached. Auto-shutoff activated!");
                this.mode = "OFF";
                currentTemp += thermalPull;
            }
        } else if ("COOLING".equals(mode)) {
            if (currentTemp > 15.0) {
                currentTemp -= 0.5;
            } else {
                System.out.println("[Thermostat WARNING] Min limit 15°C reached. Auto-shutoff activated!");
                this.mode = "OFF";
                currentTemp += thermalPull;
            }
        } else {

            currentTemp += thermalPull;
        }


        currentTemp = Math.round(currentTemp * 10.0) / 10.0;


        return (oldTemp != currentTemp) || (!oldMode.equals(this.mode));
    }

    public synchronized double getCurrentTemp() { return currentTemp; }
    public synchronized double getTargetTemp() { return targetTemp; }
    public synchronized double getOutdoorTemp() { return outdoorTemp; }
    public synchronized boolean isAutoMode() { return autoMode; }
    public synchronized String getMode() { return mode; }
}