package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class WindowReactor extends BaseReactor {
    private final WindowActuator window;
    private final DashboardController controller;

    public WindowReactor(String compId, ConsequencePool pool, DashboardController controller) {
        super(compId, pool);
        this.window = new WindowActuator(compId);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            window.execute(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Window] rejected: " + e.getMessage());
        }

        controller.updateWindowStatus(window.isOpen(), window.getShadeLevel());
    }
}