package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;

public class PlugActuator extends Actuator {
    private final int ratedWatts;
    private boolean on = false;

    public PlugActuator(String compId, int ratedWatts) {
        super(compId);
        this.ratedWatts = ratedWatts;
    }

    @Override
    protected synchronized void performAction(Event event) {
        switch (event.getType()) {
            case "TURN_ON" -> on = true;
            case "TURN_OFF" -> on = false;
            case "TOGGLE" -> on = !on;
            default -> throw new IllegalArgumentException(
                    "Plug does not support event type: " + event.getType());
        }
    }

    public synchronized boolean isOn() {
        return on;
    }

    public synchronized int getPowerDraw() {
        return on ? ratedWatts : 0;
    }
}