package hu.smarthouse.smarthouse.model;

import lombok.NonNull;

public abstract class Actuator extends SmartUnit {

    protected Actuator(String compId) {
        super(compId);
    }

    public final void execute(@NonNull Event event) {

        if (!getCompId().equals(event.getCompId())) {
            throw new IllegalArgumentException(
                    "Event targets " + event.getCompId()
                            + ", but this actuator is " + getCompId()
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