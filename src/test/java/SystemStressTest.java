import hu.smarthouse.smarthouse.core.*;
import hu.smarthouse.smarthouse.model.Event;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SystemStressTest {

    @Test
    void concurrentFloodPreservesAllEventsAndPriorityOrder() throws InterruptedException {
        BlockingQueue<Event> monitorFeed = new LinkedBlockingQueue<>();
        EventPool eventPool = new EventPool(monitorFeed);

        int threads = 50;
        int eventsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger sent = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final int id = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < eventsPerThread; j++) {
                        // Mix background telemetry, user overrides, and emergency events
                        Event e = (j % 10 == 0)
                                ? new Event("LEAK_DETECTED", "Pipe burst " + id, "HOUSE_WATER_PIPES")
                                : new Event("UPDATE_DEVICE_LOAD", "LIVING_ROOM_LINE:DEV_" + id + ":0.1", "MAIN_POWER_BREAKER");
                        eventPool.put(e);
                        sent.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "EventPool deadlocked or timed out");
        executor.shutdownNow();

        assertEquals(sent.get(), eventPool.size(), "No events should be dropped under contention");

        // Verify PriorityBlockingQueue drains EMERGENCY events first
        Event first = eventPool.take();
        assertEquals(Priority.EMERGENCY, first.getPriority());
    }

    @Test
    void manualHoldBlocksAutomationAndEmergencyOverridesBoth() {
        CommandGate gate = new CommandGate();
        long now = 10_000L;

        // 1. User manually turns on TV -> 30s hold starts
        Event userOn = new Event("TURN_ON", "User", "LIVING_ROOM_TV", Priority.USER_OVERRIDE);
        assertNull(gate.blockReason(userOn, now));

        // 2. Automation tries to turn off TV 5s later -> blocked
        Event autoOff = new Event("TURN_OFF", "Auto", "LIVING_ROOM_TV", Priority.AUTOMATION);
        assertNotNull(gate.blockReason(autoOff, now + 5_000));

        // 3. Automation tries after 30s hold expires -> allowed
        assertNull(gate.blockReason(autoOff, now + 31_000));

        // 4. Emergency lock overrides everything until RESOLVE_LEAK
        gate.lock("BATHROOM_BOILER");
        Event userBoiler = new Event("SET_BOILER_MODE", "BOOST", "BATHROOM_BOILER", Priority.USER_OVERRIDE);
        assertNotNull(gate.blockReason(userBoiler, now + 32_000));

        Event resolve = new Event("RESOLVE_LEAK", "Fixed", "HOUSE_WATER_PIPES", Priority.EMERGENCY);
        assertNull(gate.blockReason(resolve, now + 33_000));
        assertNull(gate.blockReason(userBoiler, now + 34_000));
    }
}