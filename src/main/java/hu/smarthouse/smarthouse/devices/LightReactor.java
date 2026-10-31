package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.Protocol;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class LightReactor extends BaseReactor {
    private final LightActuator light;
    private final EventPool eventPool;
    private final DashboardController controller;

    public LightReactor(String compId, ConsequencePool pool, EventPool eventPool, DashboardController controller) {
        super(compId, pool);
        this.light = new LightActuator(compId);
        this.eventPool = eventPool;
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        if (Protocol.isTelemetry(event.getType())) return;

        try {
            light.execute(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[" + getComponentId() + "] rejected: " + e.getMessage());
            return;
        }

        controller.updateLightStatus(getComponentId(), light.isOn());

        try {
            eventPool.put(new Event(Protocol.LIGHT_STATUS,
                    Protocol.encode("state", light.isOn() ? "ON" : "OFF"),
                    getComponentId()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}