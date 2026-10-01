package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.concurrent.BlockingQueue;

public abstract class BaseReactor implements Reactor {

    private final String componentId;

    private final BlockingQueue<Event> consequencePool;
    private volatile boolean isRunning = true;

    public BaseReactor(String componentId, BlockingQueue<Event> consequencePool) {
        this.componentId = componentId;
        this.consequencePool = consequencePool;
    }

    @Override
    public String getComponentId() {
        return this.componentId;
    }

    @Override
    public void run() {

        while (isRunning) {
            try {

                Event event = consequencePool.take();


                if (event.getCompID().equals(this.componentId)) {
                    react(event);
                } else {

                    consequencePool.put(event);

                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Reactor " + componentId + " stopped.");
                break;
            }
        }
    }


    public void stop() {
        this.isRunning = false;
    }
}