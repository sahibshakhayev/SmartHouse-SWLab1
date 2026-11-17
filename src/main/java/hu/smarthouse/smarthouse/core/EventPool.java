package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class EventPool {

    private record Entry(Event event, long seq) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry o) {
            int byPriority = o.event.getPriority().compareTo(event.getPriority()); // higher first
            return byPriority != 0 ? byPriority : Long.compare(seq, o.seq);        // then FIFO
        }
    }

    private final PriorityBlockingQueue<Entry> queue = new PriorityBlockingQueue<>();
    private final AtomicLong sequence = new AtomicLong();
    private final BlockingQueue<Event> monitorFeed;

    public EventPool(BlockingQueue<Event> monitorFeed) {
        this.monitorFeed = Objects.requireNonNull(monitorFeed, "monitorFeed");
    }

    public void put(Event event) throws InterruptedException {
        Event resolved = event.withPriority(PriorityMatrix.resolve(event));
        queue.put(new Entry(resolved, sequence.incrementAndGet()));
        monitorFeed.offer(resolved);          // Monitor now sees the final tier
    }

    public boolean offer(Event event) {
        Event resolved = event.withPriority(PriorityMatrix.resolve(event));
        boolean added = queue.offer(new Entry(resolved, sequence.incrementAndGet()));
        if (added) monitorFeed.offer(resolved);
        return added;
    }

    public Event take() throws InterruptedException {
        return queue.take().event();
    }

    public int size() { return queue.size(); }
}