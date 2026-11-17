package hu.smarthouse.smarthouse.core;

import hu.smarthouse.smarthouse.model.Event;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.util.concurrent.BlockingQueue;

/**
 * Background Monitor task to track and log all generated events.
 */
public class Monitor implements Runnable {


    private final BlockingQueue<Event> monitorFeed;
    private final TextArea logArea;
    private volatile boolean isRunning = true;

    public Monitor(BlockingQueue<Event> monitorFeed, TextArea logArea) {
        this.monitorFeed = monitorFeed;
        this.logArea = logArea;
    }

    @Override
    public void run() {
        Platform.runLater(() -> logArea.appendText("Monitor started. Listening to monitorFeed...\n"));

        while (isRunning) {
            try {
                // take() waits until the EventPool puts a copy of the event here
                Event event = monitorFeed.take();

                String logMessage = String.format("%1$TD %1$TT [LOG] Component: %2$s | Type: %3$s | Message: %4$s\n",
                        event.getTimestamp(), event.getCompId(), event.getType(), event.getMessage());

                Platform.runLater(() -> logArea.appendText(logMessage));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> logArea.appendText("Monitor stopped.\n"));
                break;
            }
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}