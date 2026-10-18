package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class WindowReactor extends BaseReactor {
    private final WindowActuator window = new WindowActuator("LIVING_ROOM_WINDOW");
    private final DashboardController controller;

    public WindowReactor(ConsequencePool pool, DashboardController controller) {
        super("LIVING_ROOM_WINDOW", pool);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            window.execute(event);
            controller.updateWindowStatus(window.isOpen(), window.getShadeLevel());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Window] rejected: " + e.getMessage());
        }
    }
}