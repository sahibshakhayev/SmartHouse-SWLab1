package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;

import java.io.Serial;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventPool extends LinkedBlockingQueue<Event> {

    @Serial
    private static final long serialVersionUID = 1L;
    private final transient BlockingQueue<Event> monitorFeed;

    public EventPool(BlockingQueue<Event> monitorFeed) {
        super();
        this.monitorFeed = monitorFeed;
    }

    @Override
    public void put(Event event) throws InterruptedException {
        super.put(event);
        monitorFeed.offer(event);
    }

    @Override
    public boolean offer(Event event) {
        boolean added = super.offer(event);
        if (added) monitorFeed.offer(event);
        return added;
    }
}