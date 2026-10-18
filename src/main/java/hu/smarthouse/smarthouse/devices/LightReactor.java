package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class LightReactor extends BaseReactor {
    private final DashboardController controller;
    private final String localCompID;
    private boolean isOn = false;

    public LightReactor(String compID, ConsequencePool pool, DashboardController controller) {
        super(compID, pool);
        this.localCompID = compID;
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        switch (event.getType()) {
            case "TURN_ON" -> isOn = true;
            case "TURN_OFF" -> isOn = false;
            case "TOGGLE" -> isOn = !isOn;
        }


        if ("KITCHEN_LIGHT".equals(this.localCompID)) {
            controller.updateKitchenLightStatus(isOn);
        } else {
            controller.updateLightStatus(isOn);
        }
    }
}