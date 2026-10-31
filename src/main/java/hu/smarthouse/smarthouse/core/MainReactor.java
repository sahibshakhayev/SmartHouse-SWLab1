package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;


public class MainReactor implements Runnable {

    private final EventPool eventPool;
    private final ConsequencePool consequencePool;
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

                Event triggeredEvent = eventPool.take();


                System.out.println("MainReactor routing events for component: " + triggeredEvent.getCompId());
                consequencePool.put(triggeredEvent);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(" MainReactor stoped.");
                break;
            }
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}