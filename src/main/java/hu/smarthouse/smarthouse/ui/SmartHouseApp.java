package hu.smarthouse.smarthouse.ui;

import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.MainReactor;
import hu.smarthouse.smarthouse.core.Monitor;
import hu.smarthouse.smarthouse.devices.DoorReactor;
import hu.smarthouse.smarthouse.devices.FanReactor;
import hu.smarthouse.smarthouse.devices.LightReactor;
import hu.smarthouse.smarthouse.devices.WindowReactor;
import hu.smarthouse.smarthouse.model.Event;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class SmartHouseApp extends Application {

    private ExecutorService threadPool;
    private Monitor systemMonitor;
    private MainReactor mainReactor;
    private LightReactor lightReactor;
    private FanReactor fanReactor;
    private WindowReactor windowReactor;
    private DoorReactor doorReactor;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hu/smarthouse/smarthouse/dashboard.fxml"));
        Parent root = loader.load();


        DashboardController controller = loader.getController();


        BlockingQueue<Event> monitorFeed = new LinkedBlockingQueue<>();
        EventPool eventPool = new EventPool(monitorFeed);
        ConsequencePool consequencePool = new ConsequencePool();


        controller.setEventPool(eventPool);


        mainReactor = new MainReactor(eventPool, consequencePool);
        systemMonitor = new Monitor(monitorFeed, controller.getLogArea());
        lightReactor = new LightReactor(consequencePool, controller);
        fanReactor = new FanReactor(consequencePool, controller);

        windowReactor = new WindowReactor(consequencePool, controller);
        doorReactor = new DoorReactor(consequencePool, controller);


        threadPool = Executors.newFixedThreadPool(10);
        threadPool.execute(mainReactor);
        threadPool.execute(systemMonitor);
        threadPool.execute(lightReactor);
        threadPool.execute(fanReactor);
        threadPool.execute(windowReactor);
        threadPool.execute(doorReactor);


        primaryStage.setTitle("Smart Bathroom Control");
        primaryStage.setScene(new Scene(root, 950, 650));
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (systemMonitor != null) systemMonitor.stop();
        if (mainReactor != null) mainReactor.stop();
        if (lightReactor != null) lightReactor.stop();
        if (fanReactor != null) fanReactor.stop();
        if (windowReactor != null) windowReactor.stop();
        if (doorReactor != null) doorReactor.stop();
        if (threadPool != null) threadPool.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}