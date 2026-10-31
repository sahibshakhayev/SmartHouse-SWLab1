package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;

public class WindowActuator extends Actuator {

    private boolean open = false;
    private int shadeLevel = 0; // 0 = fully raised, 100 = fully lowered

    public WindowActuator(String compId) {
        super(compId);
    }

    @Override
    protected synchronized void performAction(Event event) {
        System.out.println("Window Actuator: " + event.getMessage());
        switch (event.getType()) {
            case "OPEN" -> open = true;
            case "CLOSE" -> open = false;
            case "SHADE_UP" -> shadeLevel = shadeLevel < 100 ? shadeLevel + 1 : shadeLevel;
            case "SHADE_DOWN" -> shadeLevel = shadeLevel > 0 ? shadeLevel - 1 : shadeLevel;
            case "SHADE_SET" -> shadeLevel = Math.max(0, Math.min(100, Integer.parseInt(event.getMessage())));
            default -> throw new IllegalArgumentException(
                    "Window does not support event type: " + event.getType());
        }
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized int getShadeLevel() {
        return shadeLevel;
    }
}