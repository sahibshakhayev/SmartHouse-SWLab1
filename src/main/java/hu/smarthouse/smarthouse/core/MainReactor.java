package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.concurrent.BlockingQueue;


public class MainReactor implements Runnable {

    private final BlockingQueue<Event> eventPool;
    private final BlockingQueue<Event> consequencePool;
    private volatile boolean isRunning = true;

    public MainReactor(BlockingQueue<Event> eventPool, BlockingQueue<Event> consequencePool) {
        this.eventPool = eventPool;
        this.consequencePool = consequencePool;
    }

    @Override
    public void run() {
        System.out.println("MainReactor running. Pending events...");

        while (isRunning) {
            try {

                Event triggeredEvent = eventPool.take();


                System.out.println("MainReactor routing events for component: " + triggeredEvent.getCompID());
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