package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class WindowReactor extends BaseReactor {
    private final WindowActuator window;
    private final DashboardController controller;
    private EventPool eventPool;

    public WindowReactor(String compId, EventPool eventPool, ConsequencePool pool, DashboardController controller) {
        super(compId, pool);
        this.window = new WindowActuator(compId);
        this.eventPool = eventPool;
        this.controller = controller;

    }

    @Override
    public void react(Event event) {
        try {
            if ("DARKNESS_DETECTED".equals(event.getType())) {

                if (window.getShadeLevel() < 100) {
                    window.execute(new Event("SHADE_SET", "100", "LIVING_ROOM_WINDOW"));
                    System.out.println("[Taskflow] Night mode: Shades automatically closed.");
                }


                try {
                    eventPool.put(new Event("TURN_ON", "Night mode light activation", "LIVING_ROOM_LIGHT"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                window.execute(event);
            }

            controller.updateWindowStatus(window.isOpen(), window.getShadeLevel());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Window] rejected: " + e.getMessage());
        }
    }
}