package hu.smarthouse.smarthouse.model;

import lombok.NonNull;

public abstract class Actuator extends SmartUnit {

    protected Actuator(String compID) {
        super(compID);
    }

    public final void execute(@NonNull Event event) {

        if (!getCompID().equals(event.getCompID())) {
            throw new IllegalArgumentException(
                    "Event targets " + event.getCompID()
                            + ", but this actuator is " + getCompID()
            );
        }

        performAction(event);
    }

    /**
     * Implements the simulated action.
     * Concrete actuators should reject unsupported event types.
     */
    protected abstract void performAction(Event event);
}