package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.*;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PowerBreakerReactor extends BaseReactor {

    private static final String ID = "MAIN_POWER_BREAKER";
    private static final List<String> LINES = List.of("KITCHEN_LINE", "BATHROOM_LINE", "LIVING_ROOM_LINE");
    private static final long SETTLE_MS = 1500;   // time for devices to report their new load

    private final PowerBreakerActuator breaker;
    private final DashboardController controller;
    private final EventPool eventPool;
    private final ScheduledExecutorService rechecker = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, Long> shedAt = new HashMap<>();   // only touched from react()
    private long lastShedAt = 0;

    public PowerBreakerReactor(EventPool eventPool, ConsequencePool pool, DashboardController controller) {
        super(ID, pool);
        this.eventPool = eventPool;
        this.controller = controller;
        this.breaker = new PowerBreakerActuator(ID);
    }

    @Override
    public void react(Event event) {
        try {
            String type = event.getType();
            String payload = event.getMessage();

            breaker.execute(event);

            if ("TOGGLE_LINE".equals(type)) {
                String[] parts = payload.split(":");
                if (parts.length == 2) {
                    if (Boolean.parseBoolean(parts[1])) shedAt.remove(parts[0]);   // user restored this feeder
                    else cutPowerToLine(parts[0]);
                }
            } else if ("TOGGLE_MAIN".equals(type) && !Boolean.parseBoolean(payload)) {
                LINES.forEach(this::cutPowerToLine);
            }

            if ("UPDATE_LINE_LOAD".equals(type) || "UPDATE_DEVICE_LOAD".equals(type)) {
                guardDeadLine(payload);
            }

            checkPowerOverload();   // also runs for LOAD_RECHECK

            controller.updatePowerBreakerStatus(
                    breaker.isMainBreakerOn(),
                    breaker.getTotalPowerLoadKw(),
                    breaker.getLines(),
                    breaker.getLineLoadsKw());
        } catch (Exception e) {
            System.out.println("[PowerBreakerReactor] Exception: " + e.getMessage());
        }
    }

    /** A device reporting load on a dead line is switched off again. */
    private void guardDeadLine(String payload) {
        String[] p = payload.split(":");
        if (p.length < 2) return;
        String line = p[0];
        double load = Double.parseDouble(p[p.length - 1]);
        if (load > 0.0 && !breaker.isLineActive(line)) {
            System.out.println("[PowerBreaker Guard] " + line + " is OFF. Rejecting power request.");
            turnOffLineDevices(line);
        }
    }

    private void checkPowerOverload() {
        double load = breaker.getTotalPowerLoadKw();
        if (load <= PowerBreakerActuator.MAX_CAPACITY_KW) return;

        long now = System.currentTimeMillis();
        if (now - lastShedAt < SETTLE_MS) return;   // previous step still settling; a recheck is scheduled

        List<String> step = ShedPlanner.nextStep(
                id -> now - shedAt.getOrDefault(id, 0L) < ShedPlanner.SHED_LOCK_MS,
                breaker::isLineActive);

        if (step.isEmpty()) {
            System.out.printf("[POWER SHEDDING] %.2f kW over the limit, nothing left to shed%n", load);
            return;
        }

        System.out.printf("[POWER SHEDDING] %.2f kW > %.1f kW. Shedding: %s%n",
                load, PowerBreakerActuator.MAX_CAPACITY_KW, step);

        lastShedAt = now;
        step.forEach(id -> shedAt.put(id, now));

        publish(new Event("LOAD_SHED", String.join(",", step), ID, Priority.EMERGENCY)); // lock first
        for (String id : step) {
            if (ShedPlanner.isFeeder(id)) shedLine(id);
            else sendTurnOff(id);
        }
        scheduleRecheck();
    }

    private void shedLine(String line) {
        breaker.execute(new Event("TOGGLE_LINE", line + ":false", ID));
        cutPowerToLine(line);
        publish(new Event("POWER_LINE_SHEDDED", line, ID));
    }

    private void cutPowerToLine(String line) {
        breaker.clearLine(line);
        turnOffLineDevices(line);
    }

    private void turnOffLineDevices(String line) {
        switch (line) {
            case "LIVING_ROOM_LINE" -> {
                sendTurnOff("LIVING_ROOM_LIGHT");
                sendTurnOff("LIVING_ROOM_THERMOSTAT");
                sendTurnOff("LIVING_ROOM_TV");
                sendTurnOff("LIVING_ROOM_MUSIC");
            }
            case "KITCHEN_LINE" -> {
                sendTurnOff("KITCHEN_LIGHT");
                sendTurnOff("KITCHEN_PLUG");
            }
            case "BATHROOM_LINE" -> {
                sendTurnOff("BATHROOM_LIGHT");
                sendTurnOff("BATHROOM_BOILER");
                sendTurnOff("BATHROOM_PLUG");
                sendTurnOff("BATHROOM_FAN");
            }
        }
    }

    private void sendTurnOff(String targetId) {
        // emergency tier: a manual hold must not be able to block a protective shutdown
        publish(new Event("TURN_OFF", "Power shed / line dead", targetId, Priority.EMERGENCY));
    }

    private void scheduleRecheck() {
        rechecker.schedule(() -> publish(new Event("LOAD_RECHECK", "post-shed check", ID)),
                SETTLE_MS + 200, TimeUnit.MILLISECONDS);
    }

    private void publish(Event event) {
        try {
            eventPool.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        super.stop();
        rechecker.shutdownNow();
    }

    public PowerBreakerActuator getActuator() { return breaker; }
}