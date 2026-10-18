package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class WaterReactor extends BaseReactor {
    private final WaterActuator waterSystem = new WaterActuator("HOUSE_WATER_PIPES");
    private final DashboardController controller;

    public WaterReactor(ConsequencePool pool, DashboardController controller) {
        super("HOUSE_WATER_PIPES", pool);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            waterSystem.execute(event);
            controller.updateWaterStatus(waterSystem.isValveOpen(), waterSystem.isLeakDetected());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Water Pipes] rejected: " + e.getMessage());
            // Optional: send error to UI if needed
        }
    }
}