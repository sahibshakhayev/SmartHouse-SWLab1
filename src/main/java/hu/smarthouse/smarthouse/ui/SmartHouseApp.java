package hu.smarthouse.smarthouse.ui;

import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.MainReactor;
import hu.smarthouse.smarthouse.core.Monitor;
import hu.smarthouse.smarthouse.devices.*;
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
    private LightReactor bathRoomlightReactor;
    private LightReactor kitchenLightReactor;
    private LightReactor livingRoomLightReactor;
    private FanReactor fanReactor;
    private ThermostatReactor thermostatReactor;
    private WaterReactor waterReactor;
    private WindowReactor livingRoomWindowReactor;
    private DoorReactor livingRoomDoorReactor;
    private PlugReactor kitchenPlugReactor;
    private MotionReactor bathRoomMotionReactor;
    private PlugReactor bathRoomPlugReactor;
    private MotionSensor bathRoomMotionSensor;

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
// Обновите старый вызов:
        bathRoomlightReactor = new LightReactor("BATHROOM_LIGHT", consequencePool, eventPool, controller);
        bathRoomPlugReactor = new PlugReactor("BATHROOM_PLUG", 2200, consequencePool, eventPool, controller);
        bathRoomMotionReactor = new MotionReactor("BATHROOM_MOTION", "BATHROOM_LIGHT", consequencePool, eventPool);
        bathRoomMotionSensor = new MotionSensor("BATHROOM_MOTION");

        kitchenLightReactor = new LightReactor("KITCHEN_LIGHT", consequencePool, eventPool, controller);
        kitchenPlugReactor = new PlugReactor("KITCHEN_PLUG", 2200, consequencePool, eventPool, controller);

        controller.setBathRoomSensor(bathRoomMotionSensor);

        fanReactor = new FanReactor(consequencePool, controller);

        thermostatReactor = new ThermostatReactor(consequencePool, controller, eventPool);
        waterReactor = new WaterReactor(consequencePool, controller);

        livingRoomLightReactor = new LightReactor("LIVING_ROOM_LIGHT", consequencePool, eventPool, controller);
        livingRoomWindowReactor = new WindowReactor("LIVING_ROOM_WINDOW", eventPool, consequencePool, controller);
        livingRoomDoorReactor = new DoorReactor("FRONT_DOOR", consequencePool, controller);


        threadPool = Executors.newFixedThreadPool(16);
        threadPool.execute(mainReactor);
        threadPool.execute(systemMonitor);
        threadPool.execute(bathRoomlightReactor);
        threadPool.execute(bathRoomPlugReactor);
        threadPool.execute(bathRoomMotionReactor);
        threadPool.execute(kitchenLightReactor);
        threadPool.execute(kitchenPlugReactor);
        threadPool.execute(fanReactor);
        threadPool.execute(thermostatReactor);
        threadPool.execute(waterReactor);
        threadPool.execute(livingRoomWindowReactor);
        threadPool.execute(livingRoomDoorReactor);
        threadPool.execute(livingRoomLightReactor);

        primaryStage.setTitle("Smart Bathroom Control");
        primaryStage.setScene(new Scene(root, 950, 650));
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (systemMonitor != null) systemMonitor.stop();
        if (mainReactor != null) mainReactor.stop();
        if (bathRoomlightReactor != null) bathRoomlightReactor.stop();
        if (bathRoomPlugReactor != null) bathRoomPlugReactor.stop();
        if (kitchenLightReactor != null) kitchenLightReactor.stop();
        if (fanReactor != null) fanReactor.stop();
        if (thermostatReactor != null) thermostatReactor.stop();
        if (waterReactor != null) waterReactor.stop();
        if (livingRoomWindowReactor != null) livingRoomWindowReactor.stop();
        if (kitchenPlugReactor != null) kitchenPlugReactor.stop();
        if (bathRoomMotionReactor != null) bathRoomMotionReactor.stop();
        if (livingRoomDoorReactor != null) livingRoomDoorReactor.stop();
        if (livingRoomLightReactor != null) livingRoomLightReactor.stop();
        if (threadPool != null) threadPool.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}