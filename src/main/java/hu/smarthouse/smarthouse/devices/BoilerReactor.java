package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BoilerReactor extends BaseReactor {
    private final BoilerActuator boiler = new BoilerActuator("BATHROOM_BOILER");
    private final DashboardController controller;
    private final EventPool eventPool;
    private final ScheduledExecutorService tempSimulationThread = Executors.newSingleThreadScheduledExecutor();

    public BoilerReactor(ConsequencePool pool, EventPool eventPool, DashboardController controller) {
        super("BATHROOM_BOILER", pool);
        this.eventPool = eventPool;
        this.controller = controller;

        startWaterTempSimulation();
    }

    @Override
    public void react(Event event) {
        try {
            String type = event.getType();


            if ("TURN_OFF".equals(type)) {
                boiler.execute(event);
                eventPool.put(new Event("UPDATE_LINE_LOAD", "BATHROOM_LINE:0.0", "MAIN_POWER_BREAKER"));
                updateUI();
                return;
            }


            if ("WATER_VALVE_CLOSED".equals(type)) {
                boiler.execute(event);
                System.out.println("[Boiler WARNING] Water supply shut off! Dry-run protection triggered.");
                eventPool.put(new Event("UPDATE_LINE_LOAD", "BATHROOM_LINE:0.0", "MAIN_POWER_BREAKER"));
                updateUI();
                return;
            }


            boiler.execute(event);


            double loadKw = boiler.getPowerDrawKw();
            eventPool.put(new Event("UPDATE_LINE_LOAD", "BATHROOM_LINE:" + loadKw, "MAIN_POWER_BREAKER"));
            updateUI();

        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("[Boiler] Rejected command: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void startWaterTempSimulation() {
        tempSimulationThread.scheduleAtFixedRate(() -> {
            boolean updated = boiler.simulateTemperatureStep();
            if (updated) {
                updateUI();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateUI() {
        controller.updateBoilerStatus(
                boiler.getMode(),
                boiler.getCurrentWaterTemp(),
                boiler.getTargetTemp(),
                boiler.getPowerDrawKw(),
                boiler.isWaterSupplyAvailable()
        );
    }

    @Override
    public void stop() {
        super.stop();
        tempSimulationThread.shutdownNow();
    }
}