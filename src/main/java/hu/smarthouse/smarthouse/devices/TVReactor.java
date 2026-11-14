package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class TVReactor extends BaseReactor {
    private static final String ID = "LIVING_ROOM_TV";
    private static final String LINE = "LIVING_ROOM_LINE";

    private final TVActuator tv = new TVActuator(ID);
    private final EventPool eventPool;
    private final DashboardController controller;

    public TVReactor(ConsequencePool pool, EventPool eventPool, DashboardController controller) {
        super(ID, pool);
        this.eventPool = eventPool;
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            tv.execute(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[TV] rejected: " + e.getMessage());
            return;
        }

        controller.updateTvStatus(tv.isOn(), tv.getChannel(), tv.getVolume());

        try {
            eventPool.put(new Event("UPDATE_DEVICE_LOAD",
                    LINE + ":" + ID + ":" + tv.getPowerDrawKw(), "MAIN_POWER_BREAKER"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}