package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class DoorReactor extends BaseReactor {
    private final DoorActuator door = new DoorActuator("FRONT_DOOR");
    private final DashboardController controller;

    public DoorReactor(ConsequencePool pool, DashboardController controller) {
        super("FRONT_DOOR", pool);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            System.out.println("Check door: " + event.getMessage());
            door.execute(event);
            controller.updateDoorStatus(door.isOpen(), door.isLocked());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Door] rejected: " + e.getMessage());
        }
    }
}
