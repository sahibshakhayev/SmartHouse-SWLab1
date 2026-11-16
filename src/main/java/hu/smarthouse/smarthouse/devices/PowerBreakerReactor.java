package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.*;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

import java.util.List;

public class PowerBreakerReactor extends BaseReactor {
    private static final String ID = "MAIN_POWER_BREAKER";
    private static final List<String> LINES = List.of("KITCHEN_LINE", "BATHROOM_LINE", "LIVING_ROOM_LINE");

    private final PowerBreakerActuator breaker;
    private final DashboardController controller;
    private final EventPool eventPool;

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

            if ("UPDATE_DEVICE_LOAD".equals(type)) {
                handleDeviceLoadRequest(payload);
            } else {
                breaker.execute(event);

                if ("TOGGLE_LINE".equals(type)) {
                    String[] parts = payload.split(":");
                    if (parts.length == 2 && !Boolean.parseBoolean(parts[1])) {
                        cutPowerToLine(parts[0]);
                    }
                } else if ("TOGGLE_MAIN".equals(type) && !Boolean.parseBoolean(payload)) {
                    LINES.forEach(this::cutPowerToLine);
                }
            }

            controller.updatePowerBreakerStatus(
                    breaker.isMainBreakerOn(),
                    breaker.getTotalPowerLoadKw(),
                    breaker.getLines(),
                    breaker.getLineLoadsKw()
            );
        } catch (Exception e) {
            System.out.println("[PowerBreakerReactor] Exception: " + e.getMessage());
        }
    }

    private void handleDeviceLoadRequest(String payload) {
        String[] parts = payload.split(":");
        if (parts.length != 3) return;

        String line = parts[0];
        String deviceId = parts[1];
        double requestedKw = Math.max(0.0, Double.parseDouble(parts[2]));

        // Turning OFF (0.0 kW) is always allowed
        if (requestedKw == 0.0) {
            breaker.execute(new Event("UPDATE_DEVICE_LOAD", payload, ID));
            return;
        }

        // Check 1: Is the feeder line energized?
        if (!breaker.isLineEnergized(line)) {
            String errorMsg = !breaker.isMainBreakerOn()
                    ? "Cannot power on " + deviceId + ": MAIN BREAKER is OFF."
                    : "Cannot power on " + deviceId + ": " + line + " is OFF.";
            rejectDevicePowerOn(deviceId, errorMsg);
            return;
        }

        // Check 2: Is there enough remaining capacity within the 5.00 kW limit?
        double previousKw = breaker.getDeviceLoadKw(line, deviceId);
        double availableForDevice = breaker.getRemainingCapacityKw() + previousKw;

        if (requestedKw > availableForDevice + 1e-6) {
            String errorMsg = String.format(
                    "Insufficient power capacity for %s (needs %.2f kW, only %.2f kW remaining of %.2f kW).",
                    deviceId, requestedKw, availableForDevice, PowerBreakerActuator.MAX_CAPACITY_KW);
            rejectDevicePowerOn(deviceId, errorMsg);
            return;
        }

        // Admitted: commit load to breaker
        breaker.execute(new Event("UPDATE_DEVICE_LOAD", payload, ID));
    }

    private void rejectDevicePowerOn(String deviceId, String errorMsg) {
        System.err.println("[PowerBreaker REJECTED] " + errorMsg);

        // 1. Force the device back OFF (EMERGENCY priority bypasses 30s manual hold)
        publish(new Event("TURN_OFF", "Rejected: insufficient power capacity", deviceId, Priority.EMERGENCY));

        // 2. Record in System Monitor Log
        publish(new Event("CAPACITY_EXCEEDED", errorMsg, "SYSTEM_MONITOR", Priority.EMERGENCY));

        // 3. Display visible error banner in GUI
        controller.showPowerError(errorMsg);
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
                sendTurnOff("BATHROOM_FAN");
                sendTurnOff("BATHROOM_PLUG");
                sendTurnOff("BATHROOM_BOILER");
            }
        }
    }

    private void sendTurnOff(String targetId) {
        publish(new Event("TURN_OFF", "Power line dead", targetId, Priority.EMERGENCY));
    }

    private void publish(Event event) {
        try {
            eventPool.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public PowerBreakerActuator getActuator() {
        return breaker;
    }
}