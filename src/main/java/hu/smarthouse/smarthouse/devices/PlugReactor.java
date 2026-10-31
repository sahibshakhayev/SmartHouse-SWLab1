package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.Protocol;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class PlugReactor extends BaseReactor {
    private final PlugActuator plug;
    private final EventPool eventPool;
    private final DashboardController controller;

    public PlugReactor(String compId, int ratedWatts, ConsequencePool pool,
                       EventPool eventPool, DashboardController controller) {
        super(compId, pool);
        this.plug = new PlugActuator(compId, ratedWatts);
        this.eventPool = eventPool;
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        if (Protocol.isTelemetry(event.getType())) return;

        try {
            plug.execute(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[" + getComponentId() + "] rejected: " + e.getMessage());
            return;
        }

        controller.updatePlugStatus(getComponentId(), plug.isOn(), plug.getPowerDraw());

        try {
            eventPool.put(new Event(Protocol.PLUG_STATUS,
                    Protocol.encode("state", plug.isOn() ? "ON" : "OFF",
                            "watts", String.valueOf(plug.getPowerDraw())),
                    getComponentId()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}