package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.Protocol;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.model.Sensor;
import lombok.Setter;

@Setter
public class MotionSensor extends Sensor {
    private volatile boolean motion = false;

    public MotionSensor(String compId) {
        super(compId);
    }

    @Override
    public Event sense() {
        boolean m = motion;
        return createEvent(
                m ? Protocol.MOTION_DETECTED : Protocol.MOTION_CLEARED,
                Protocol.encode("detected", m ? "1" : "0"));
    }
}