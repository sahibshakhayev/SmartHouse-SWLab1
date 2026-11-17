package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;

public class LightActuator extends Actuator {
    private boolean on = false;

    public LightActuator(String compId) {
        super(compId);
    }

    @Override
    protected synchronized void performAction(Event event) {
        switch (event.getType()) {
            case "TURN_ON" -> on = true;
            case "TURN_OFF" -> on = false;
            case "TOGGLE" -> on = !on;
            default -> throw new IllegalArgumentException(
                    "Light does not support event type: " + event.getType());
        }
    }

    public synchronized boolean isOn() {
        return on;
    }
}