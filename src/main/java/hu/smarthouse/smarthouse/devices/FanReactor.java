package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FanReactor extends BaseReactor {
    private final DashboardController controller;
    private boolean isOn = false;

    // Dedicated thread pool for handling timer events
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerTask = null;

    public FanReactor(ConsequencePool pool, DashboardController controller) {
        super("BATHROOM_FAN", pool);
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        switch (event.getType()) {
            case "TURN_ON_TIMER":
                int delayMinutes = Integer.parseInt(event.getMessage());
                isOn = true;
                controller.updateFanStatus(true);

                // Cancel existing timer to prevent conflicts
                if (timerTask != null && !timerTask.isDone()) {
                    timerTask.cancel(false);
                }

                timerTask = scheduler.schedule(() -> {
                    isOn = false;
                    controller.updateFanStatus(false);
                }, delayMinutes, TimeUnit.MINUTES);
                break;

            case "TURN_OFF":
                isOn = false;
                controller.updateFanStatus(false);
                if (timerTask != null) {
                    timerTask.cancel(false); // Cancel timer if shut down early
                }
                break;

            case "TOGGLE":
                isOn = !isOn; // Switch to the opposite state
                controller.updateFanStatus(isOn);
                // If manually toggled OFF, ensure any running timer is cancelled
                if (!isOn && timerTask != null) {
                    timerTask.cancel(false);
                }
                break;
        }
    }

    @Override
    public void stop() {
        super.stop();
        scheduler.shutdownNow(); // Clean up threads
    }
}