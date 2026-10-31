package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class DoorReactor extends BaseReactor {
    private final DoorActuator door;
    private final DashboardController controller;

    public DoorReactor(String compId, ConsequencePool pool, DashboardController controller) {
        super(compId, pool);
        this.door = new DoorActuator(compId);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            System.out.println("Check door: " + event.getMessage());
            door.execute(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Door] rejected: " + e.getMessage());
        }

        controller.updateDoorStatus(door.isOpen(), door.isLocked());
    }
}
