package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class LightReactor extends BaseReactor {
    private final DashboardController controller;
    private boolean isOn = false;

    public LightReactor(ConsequencePool pool, DashboardController controller) {
        super("BATHROOM_LIGHT", pool);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        switch (event.getType()) {
            case "TURN_ON":
                isOn = true;
                break;
            case "TURN_OFF":
                isOn = false;
                break;
            case "TOGGLE":
                isOn = !isOn; // Switch to the opposite state
                break;
        }

        // Update UI safely
        controller.updateLightStatus(isOn);
    }
}