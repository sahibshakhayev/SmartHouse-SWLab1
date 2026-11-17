package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsequencePool {

    private final ConcurrentHashMap<String, BlockingQueue<Event>> queues = new ConcurrentHashMap<>();

    private BlockingQueue<Event> queueFor(String componentId) {
        return queues.computeIfAbsent(componentId, id -> new LinkedBlockingQueue<>());
    }

    public void put(Event consequence) throws InterruptedException {
        queueFor(consequence.getCompId()).put(consequence);
    }

    public Event take(String componentId) throws InterruptedException {
        return queueFor(componentId).take();
    }
}