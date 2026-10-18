package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;

public class DoorActuator extends Actuator {

    private boolean open = false;
    private boolean locked = false;

    public DoorActuator(String compID) {
        super(compID);
    }

    @Override
    protected synchronized void performAction(Event event) {
        switch (event.getType()) {
            case "OPEN" -> {
                if (locked) throw new IllegalStateException("Door is locked");
                open = true;
            }
            case "CLOSE" -> open = false;
            case "LOCK" -> {
                if (open) throw new IllegalStateException("Cannot lock an open door");
                locked = true;
            }
            case "UNLOCK" -> locked = false;
            default -> throw new IllegalArgumentException(
                    "Door does not support event type: " + event.getType());
        }
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized boolean isLocked() {
        return locked;
    }
}