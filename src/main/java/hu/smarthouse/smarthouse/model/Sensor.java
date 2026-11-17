package hu.smarthouse.smarthouse.model;

public abstract class Sensor extends SmartUnit {

    protected Sensor(String compId) {
        super(compId);
    }

    /**
     * Reads the simulated environment and returns a non-null event.
     * Scheduling and event publication are handled externally.
     */
    public abstract Event sense();

    protected final Event createEvent(String type, String message) {
        return new Event(type, message, getCompId());
    }

}