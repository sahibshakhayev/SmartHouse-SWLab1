package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class PowerBreakerReactor extends BaseReactor {
    private final PowerBreakerActuator breaker;
    private final DashboardController controller;
    private final EventPool eventPool;

    public PowerBreakerReactor(EventPool eventPool, ConsequencePool pool, DashboardController controller) {
        super("MAIN_POWER_BREAKER", pool);
        this.eventPool = eventPool;
        this.controller = controller;
        this.breaker = new PowerBreakerActuator("MAIN_POWER_BREAKER");
    }

    @Override
    public void react(Event event) {
        try {
            String type = event.getType();
            String payload = event.getMessage();

            // 1. Process breaker state updates
            breaker.execute(event);

            // 2. If line or main breaker was toggled OFF, force-cut all connected devices
            if ("TOGGLE_LINE".equals(type)) {
                String[] parts = payload.split(":");
                if (parts.length == 2 && !Boolean.parseBoolean(parts[1])) {
                    cutPowerToLine(parts[0]);
                }
            } else if ("TOGGLE_MAIN".equals(type) && !Boolean.parseBoolean(payload)) {
                cutPowerToLine("KITCHEN_LINE");
                cutPowerToLine("BATHROOM_LINE");
                cutPowerToLine("LIVING_ROOM_LINE");
            }

            // 3. Interlock Guard: If a device sends load > 0 while its line is OFF, immediately reject it with TURN_OFF
            if ("UPDATE_LINE_LOAD".equals(type)) {
                String[] parts = payload.split(":");
                if (parts.length == 2) {
                    String lineName = parts[0];
                    double load = Double.parseDouble(parts[1]);
                    if (load > 0.0 && !breaker.isLineActive(lineName)) {
                        System.out.println("[PowerBreaker Guard] Line " + lineName + " is OFF. Rejecting power request from " + event.getCompId());
                        turnOffLineDevices(lineName);
                    }
                }
            }

            // 4. Dynamic Load Shedding check (> 5.0 kW)
            checkPowerOverload();

            // 5. Update UI
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

    private void checkPowerOverload() {
        double currentLoad = breaker.getTotalPowerLoadKw();
        if (currentLoad > PowerBreakerActuator.MAX_CAPACITY_KW) {
            System.out.printf("[POWER SHEDDING] Load %.2f kW > 5.0 kW limit! Dropping secondary line...%n", currentLoad);

            // Priority order for shedding: Living Room -> Kitchen -> Bathroom
            if (breaker.isLineActive("LIVING_ROOM_LINE")) {
                shedLine("LIVING_ROOM_LINE");
            } else if (breaker.isLineActive("KITCHEN_LINE")) {
                shedLine("KITCHEN_LINE");
            } else if (breaker.isLineActive("BATHROOM_LINE")) {
                shedLine("BATHROOM_LINE");
            }
        }
    }

    private void shedLine(String lineName) {
        breaker.execute(new Event("TOGGLE_LINE", lineName + ":false", "MAIN_POWER_BREAKER"));
        cutPowerToLine(lineName);
        try {
            eventPool.put(new Event("POWER_LINE_SHEDDED", lineName, "MAIN_POWER_BREAKER"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void cutPowerToLine(String lineName) {
        breaker.execute(new Event("UPDATE_LINE_LOAD", lineName + ":0.0", "MAIN_POWER_BREAKER"));
        turnOffLineDevices(lineName);
    }

    private void turnOffLineDevices(String lineName) {
        switch (lineName) {
            case "LIVING_ROOM_LINE" -> {
                sendTurnOff("LIVING_ROOM_LIGHT");
                sendTurnOff("LIVING_ROOM_THERMOSTAT");
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
        try {
            eventPool.put(new Event("TURN_OFF", "Power line dead", targetId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public PowerBreakerActuator getActuator() { return breaker; }
}