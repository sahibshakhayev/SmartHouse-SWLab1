package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;

import java.util.Set;

public class MainReactor implements Runnable {

    private static final String BREAKER_ID = "MAIN_POWER_BREAKER";
    private static final Set<String> POWER_TYPES = Set.of("UPDATE_LINE_LOAD", "TOGGLE_LINE");

    private final EventPool eventPool;
    private final ConsequencePool consequencePool;
    private final CommandGate gate = new CommandGate();
    private volatile boolean isRunning = true;

    public MainReactor(EventPool eventPool, ConsequencePool consequencePool) {
        this.eventPool = eventPool;
        this.consequencePool = consequencePool;
    }

    @Override
    public void run() {
        System.out.println("MainReactor running. Pending events...");

        while (isRunning) {
            try {
                route(eventPool.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("MainReactor stopped.");
                break;
            }
        }
    }

    private void route(Event event) throws InterruptedException {
        if (event.getPriority() == Priority.EMERGENCY) {
            for (Event consequence : InterlockRules.consequencesOf(event)) {
                gate.lock(consequence.getCompId());
                eventPool.put(consequence);
            }
        }

        Event routed = event;
        if (POWER_TYPES.contains(event.getType())) {
            routed = new Event(event.getType(), event.getMessage(), BREAKER_ID, event.getPriority());
            // removed: the extra "routing power event" println
        }

        if (!gate.allow(routed, System.currentTimeMillis())) {
            System.out.println("[Gate] blocked " + routed.getType() + " -> " + routed.getCompId()
                    + " (" + routed.getPriority() + ")");
            return;
        }

        System.out.printf("MainReactor [%s] %s -> %s%n",
                routed.getPriority(), routed.getType(), routed.getCompId());
        consequencePool.put(routed);
    }

    public void stop() {
        this.isRunning = false;
    }
}