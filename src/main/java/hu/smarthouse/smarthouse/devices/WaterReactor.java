package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class WaterReactor extends BaseReactor {
    private final WaterActuator waterSystem = new WaterActuator("HOUSE_WATER_PIPES");
    private final DashboardController controller;
    private final EventPool eventPool;

    public WaterReactor(ConsequencePool pool, EventPool eventPool, DashboardController controller) {
        super("HOUSE_WATER_PIPES", pool);
        this.eventPool = eventPool;
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {

            waterSystem.execute(event);


            controller.updateWaterStatus(waterSystem.isValveOpen(), waterSystem.isLeakDetected());


            if (!waterSystem.isValveOpen()) {

                eventPool.put(new Event("WATER_VALVE_CLOSED", "Water shutoff", "BATHROOM_BOILER"));
            } else if ("VALVE_OPEN".equals(event.getType()) || "RESOLVE_LEAK".equals(event.getType())) {

                eventPool.put(new Event("WATER_VALVE_OPENED", "Water restored", "BATHROOM_BOILER"));
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Water Pipes] rejected: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}