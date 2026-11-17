package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class MainReactor implements Runnable {

    private static final String BREAKER_ID = "MAIN_POWER_BREAKER";
    private static final String MONITOR_ID = "SYSTEM_MONITOR";
    private static final Set<String> POWER_TYPES = Set.of("UPDATE_LINE_LOAD", "UPDATE_DEVICE_LOAD", "TOGGLE_LINE");

    private final EventPool eventPool;
    private final ConsequencePool consequencePool;
    private final BlockingQueue<Event> monitorFeed;
    private final CommandGate gate = new CommandGate();
    private volatile boolean isRunning = true;

    public MainReactor(EventPool eventPool, ConsequencePool consequencePool, BlockingQueue<Event> monitorFeed) {
        this.eventPool = eventPool;
        this.consequencePool = consequencePool;
        this.monitorFeed = monitorFeed;
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
        if (MONITOR_ID.equals(event.getCompId())) {
            return; // Already recorded by EventPool into monitorFeed; do not queue into ConsequencePool
        }

        if (event.getPriority() == Priority.EMERGENCY) {
            for (Event consequence : InterlockRules.consequencesOf(event)) {
                gate.lock(consequence.getCompId());
                eventPool.put(consequence);
            }
        }

        Event routed = event;
        if (POWER_TYPES.contains(event.getType())) {
            routed = new Event(event.getType(), event.getMessage(), BREAKER_ID, event.getPriority());
        }

        if ("LOAD_SHED".equals(event.getType())) {
            long until = System.currentTimeMillis() + ShedPlanner.SHED_LOCK_MS;
            for (String id : event.getMessage().split(",")) {
                gate.shedLock(id.trim(), until);
            }
            monitorFeed.offer(new Event("SHED_LOCK", "Locked against automation (60s): " + event.getMessage(), MONITOR_ID, Priority.EMERGENCY));
            return;
        }

        String reason = gate.blockReason(routed, System.currentTimeMillis());
        if (reason != null) {
            String msg = String.format("Blocked %s (%s): %s", routed.getType(), routed.getPriority(), reason);
            System.out.println("[Gate] " + msg);
            monitorFeed.offer(new Event("GATE_BLOCKED", msg, routed.getCompId(), routed.getPriority()));
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