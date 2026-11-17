package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;

public class WaterActuator extends Actuator {
    private boolean mainValveOpen = true;
    private boolean leakDetected = false;

    public WaterActuator(String compId) {
        super(compId);
    }

    @Override
    protected synchronized void performAction(Event event) {
        System.out.println("Water Actuator: " + event.getMessage());
        switch (event.getType()) {
            case "VALVE_OPEN" -> {
                if (leakDetected) throw new IllegalStateException("Cannot open main valve. Leak detected!");
                mainValveOpen = true;
            }
            case "VALVE_CLOSE" -> mainValveOpen = false;
            case "TRIGGER_LEAK" -> {
                leakDetected = true;
                mainValveOpen = false; // Auto shut-off safety measure
            }
            case "RESOLVE_LEAK" -> leakDetected = false;
            default -> throw new IllegalArgumentException(
                    "Water system does not support event type: " + event.getType());
        }
    }

    public synchronized boolean isValveOpen() { return mainValveOpen; }
    public synchronized boolean isLeakDetected() { return leakDetected; }
}