package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.*;
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
            double load = light.isOn() ? 0.1 : 0.0;
            String lineName = getComponentId().contains("KITCHEN") ? "KITCHEN_LINE" :
                    getComponentId().contains("BATHROOM") ? "BATHROOM_LINE" : "LIVING_ROOM_LINE";

            try {
                eventPool.put(new Event("UPDATE_LINE_LOAD", lineName + ":" + load, "MAIN_POWER_BREAKER"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

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