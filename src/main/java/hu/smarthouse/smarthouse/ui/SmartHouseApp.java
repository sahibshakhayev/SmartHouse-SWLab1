package hu.smarthouse.smarthouse.ui;

import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.MainReactor;
import hu.smarthouse.smarthouse.core.Monitor;
import hu.smarthouse.smarthouse.model.Event;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartHouseApp extends Application {

    private ExecutorService threadPool;
    private Monitor systemMonitor;
    private MainReactor mainReactor;

    @Override
    public void start(Stage primaryStage) {
        // --- 1. UI Setup ---
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(400);

        VBox root = new VBox(10, logArea);
        Scene scene = new Scene(root, 600, 500);

        primaryStage.setTitle("Smart House Prototype");
        primaryStage.setScene(scene);
        primaryStage.show();

        // --- 2. Core Initialization (based on Asel's architecture) ---
        // Dedicated channel for the monitor
        BlockingQueue<Event> monitorFeed = new LinkedBlockingQueue<>();

        // Main pools
        EventPool eventPool = new EventPool(monitorFeed);
        ConsequencePool consequencePool = new ConsequencePool();

        // Create reactors and monitor
        mainReactor = new MainReactor(eventPool, consequencePool);
        systemMonitor = new Monitor(monitorFeed, logArea);

        // --- 3. Thread Execution ---
        // Starting with 10 threads to accommodate the main reactor, monitor, and future 7 devices
        threadPool = Executors.newFixedThreadPool(10);
        threadPool.execute(mainReactor);
        threadPool.execute(systemMonitor);

        // Future implementation: Initialize and execute the 7 specific smart devices here
    }

    @Override
    public void stop() {
        if (systemMonitor != null) systemMonitor.stop();
        if (mainReactor != null) mainReactor.stop();
        if (threadPool != null) threadPool.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}