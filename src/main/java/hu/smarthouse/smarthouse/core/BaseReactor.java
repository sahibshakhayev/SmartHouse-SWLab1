package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.concurrent.BlockingQueue;

public abstract class BaseReactor implements Reactor {

    private final String componentId;

    private final ConsequencePool consequencePool;
    private volatile boolean isRunning = true;

    public BaseReactor(String componentId, ConsequencePool consequencePool) {
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

                Event event = consequencePool.take(componentId);


                react(event);

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