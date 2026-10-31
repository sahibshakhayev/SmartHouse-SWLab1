package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.Protocol;
import hu.smarthouse.smarthouse.model.Event;

public class MotionReactor extends BaseReactor {
    private final EventPool eventPool;
    private final String targetCompId;
    private boolean lastPresent = false;

    public MotionReactor(String compId, String targetCompId,
                         ConsequencePool pool, EventPool eventPool) {
        super(compId, pool);
        this.targetCompId = targetCompId;
        this.eventPool = eventPool;
    }

    @Override
    public void react(Event event) {
        boolean present;
        switch (event.getType()) {
            case Protocol.MOTION_DETECTED -> present = true;
            case Protocol.MOTION_CLEARED -> present = false;
            default -> {
                return;
            }
        }
        if (present == lastPresent) return; // heartbeat, no change

        lastPresent = present;
        try {
            eventPool.put(new Event(present ? "TURN_ON" : "TURN_OFF",
                    "Motion " + (present ? "detected" : "cleared"), targetCompId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}