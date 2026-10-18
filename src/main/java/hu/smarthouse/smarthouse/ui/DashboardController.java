package hu.smarthouse.smarthouse.ui;

import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Objects;

public class DashboardController {

    @FXML private TextArea logArea;

    // Navigation
    @FXML private Label pageTitle;
    @FXML private GridPane bathroomView;
    @FXML private GridPane livingRoomView;
    @FXML private Button bathroomNavButton;
    @FXML private Button livingRoomNavButton;

    private static final String NAV_ACTIVE =
            "-fx-background-color: #4CAF50; -fx-text-fill: white;";
    private static final String NAV_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-border-color: #b3b3b3; -fx-border-radius: 3;";

    // Light Components
    @FXML private ImageView lightIcon;
    @FXML private Label lightStatus;

    @FXML private Button kitchenNavButton;
    @FXML private GridPane kitchenView;
    @FXML private ImageView kitchenLightIcon;
    @FXML private Label kitchenLightStatus;

    // Fan Components
    @FXML private ImageView fanIcon;
    @FXML private Label fanStatus;

    @FXML private Label indoorTempLabel;
    @FXML private Label targetTempLabel;
    @FXML private Label outdoorTempLabel;
    @FXML private Label climateModeLabel;
    @FXML private Button autoModeButton;

    @FXML private Label valveStatusLabel;
    @FXML private Label leakAlertLabel;

    // Window Components

    @FXML private ImageView windowIcon;
    @FXML private Label windowStatus;
    @FXML private Slider shadeSlider;
    
    // Door Components
    @FXML private ImageView doorIcon;
    @FXML private Label doorStatus;

    // Settings Components
    @FXML private Slider fanDelaySlider;

    private EventPool eventPool;

    // Image assets (Ensure these files are in src/main/resources/assets/)
    private Image imgLightOn;
    private Image imgLightOff;
    private Image imgFanOn; // This should be your .gif file
    private Image imgFanOff; // Static .png file
    private Image imgWindowOpen;
    private Image imgWindowClosed;
    private Image imgDoorOpen;
    private Image imgDoorClosed;
    private Image imgDoorLocked;

    @FXML
    public void initialize() {
        // Load images on startup. Try-catch blocks ensure the app doesn't crash if images are missing yet.
        try {
            imgLightOn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/light_on.png")));
            imgLightOff = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/light_off.png")));
            imgFanOn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/fan_on.gif")));
            imgFanOff = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/fan_off.png")));
            kitchenLightIcon.setImage(imgLightOff);
            imgWindowOpen = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/window_open.png")));
            imgWindowClosed = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/window_closed.png")));

            imgDoorOpen = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/door_open.png")));
            imgDoorClosed = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/door_closed.png")));
            imgDoorLocked = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/door_locked.png")));

            // Set initial off states
            lightIcon.setImage(imgLightOff);
            fanIcon.setImage(imgFanOff);
            windowIcon.setImage(imgWindowOpen);
            doorIcon.setImage(imgDoorOpen);
        } catch (NullPointerException e) {
            System.err.println("Warning: UI Assets not found in /assets/ folder.");
        }
    }

    public void setEventPool(EventPool eventPool) {
        this.eventPool = eventPool;
    }

    public TextArea getLogArea() {
        return logArea;
    }

    // --- User Interaction Events ---

    @FXML
    private void onShowBathroom() { showView("BATHROOM"); }

    @FXML
    private void onShowLivingRoom() { showView("LIVING_ROOM"); }

    @FXML
    private void onShowKitchen() { showView("KITCHEN"); }

    private void showView(String viewName) {
        boolean isBath = viewName.equals("BATHROOM");
        boolean isLiv = viewName.equals("LIVING_ROOM");
        boolean isKitch = viewName.equals("KITCHEN");

        bathroomView.setVisible(isBath); bathroomView.setManaged(isBath);
        livingRoomView.setVisible(isLiv); livingRoomView.setManaged(isLiv);
        kitchenView.setVisible(isKitch); kitchenView.setManaged(isKitch);

        if(isBath) pageTitle.setText("Bathroom Dashboard");
        else if(isLiv) pageTitle.setText("Living Room Dashboard");
        else pageTitle.setText("Kitchen Dashboard");

        bathroomNavButton.setStyle(isBath ? NAV_ACTIVE : NAV_INACTIVE);
        livingRoomNavButton.setStyle(isLiv ? NAV_ACTIVE : NAV_INACTIVE);
        kitchenNavButton.setStyle(isKitch ? NAV_ACTIVE : NAV_INACTIVE);
    }

    @FXML
    private void onToggleLight() {
        sendEvent(new Event("TOGGLE", "User toggled light", "BATHROOM_LIGHT"));
    }

    @FXML
    private void onToggleFan() {
        sendEvent(new Event("TOGGLE", "User toggled fan", "BATHROOM_FAN"));
    }

    @FXML
    private void onMotionDetected() {
        sendEvent(new Event("SENSOR_TRIGGER", "Motion detected", "BATHROOM_LIGHT"));
    }

    @FXML
    private void onPresenceDetected() {
        sendEvent(new Event("SENSOR_TRIGGER", "Presence detected", "BATHROOM_FAN"));
    }

    @FXML
    private void onToggleKitchenLight() {
        sendEvent(new Event("TOGGLE", "User toggled kitchen light", "KITCHEN_LIGHT"));
    }

    public void updateKitchenLightStatus(boolean isOn) {
        Platform.runLater(() -> {
            kitchenLightIcon.setImage(isOn ? imgLightOn : imgLightOff);
            kitchenLightStatus.setText(isOn ? "Status: ON" : "Status: OFF");
        });
    }

    @FXML
    private void onSaveSettings() {
        int delayValue = (int) fanDelaySlider.getValue();
        sendEvent(new Event("CONFIG", "Fan delay set to " + delayValue + " mins", "BATHROOM_FAN"));
    }


    @FXML
    private void onPersonEnters() {
        // Person enters: Turn ON light, Turn OFF fan (if it was running)
        sendEvent(new Event("TURN_ON", "Person entered bathroom", "BATHROOM_LIGHT"));
        sendEvent(new Event("TURN_OFF", "Person entered bathroom", "BATHROOM_FAN"));
    }

    @FXML
    private void onPersonLeaves() {
        // Get the value from the slider
        int delayMinutes = (int) fanDelaySlider.getValue();

        // Person leaves: Turn OFF light, Turn ON fan with timer
        sendEvent(new Event("TURN_OFF", "Person left bathroom", "BATHROOM_LIGHT"));
        sendEvent(new Event("TURN_ON_TIMER", String.valueOf(delayMinutes), "BATHROOM_FAN"));
    }


    // --- Thermostat Interactions ---

    @FXML
    private void onToggleAutoClimate() {
        sendEvent(new Event("TOGGLE_AUTO", "User toggled auto climate", "LIVING_ROOM_THERMOSTAT"));
    }

    @FXML
    private void onTempUp() {
        sendEvent(new Event("TEMP_UP", "User increased target temp", "LIVING_ROOM_THERMOSTAT"));
    }

    @FXML
    private void onTempDown() {
        sendEvent(new Event("TEMP_DOWN", "User decreased target temp", "LIVING_ROOM_THERMOSTAT"));
    }

    @FXML
    private void onModeHeat() {
        sendEvent(new Event("MODE_HEAT", "User set mode to HEAT", "LIVING_ROOM_THERMOSTAT"));
    }

    @FXML
    private void onModeCool() {
        sendEvent(new Event("MODE_COOL", "User set mode to COOL", "LIVING_ROOM_THERMOSTAT"));
    }

    @FXML
    private void onModeOff() {
        sendEvent(new Event("MODE_OFF", "User turned off climate control", "LIVING_ROOM_THERMOSTAT"));
    }

    // --- Water Pipes Interactions ---

    @FXML
    private void onOpenValve() {
        sendEvent(new Event("VALVE_OPEN", "User opened main valve", "HOUSE_WATER_PIPES"));
    }

    @FXML
    private void onCloseValve() {
        sendEvent(new Event("VALVE_CLOSE", "User closed main valve", "HOUSE_WATER_PIPES"));
    }

    @FXML
    private void onTriggerLeak() {
        sendEvent(new Event("TRIGGER_LEAK", "Critical leak detected by sensor", "HOUSE_WATER_PIPES"));
    }

    @FXML
    private void onResolveLeak() {
        sendEvent(new Event("RESOLVE_LEAK", "Leak resolved manually", "HOUSE_WATER_PIPES"));
    }

    public void updateThermostatStatus(double currentTemp, double targetTemp, double outdoorTemp, String mode, boolean isAuto) {
        Platform.runLater(() -> {
            if (indoorTempLabel != null) {
                indoorTempLabel.setText(String.format("Indoor: %.1f°C", currentTemp));
                targetTempLabel.setText(String.format("Target: %.1f°C", targetTemp));
                outdoorTempLabel.setText(String.format("Outdoor (API): %.1f°C", outdoorTemp));

                String modeText = isAuto ? "AUTO (" + mode + ")" : "MANUAL (" + mode + ")";
                climateModeLabel.setText("Mode: " + modeText);

                // Visually toggle Auto button state
                autoModeButton.setStyle(isAuto ? "-fx-background-color: #4CAF50; -fx-text-fill: white;" : "-fx-background-color: #9e9e9e; -fx-text-fill: white;");
            }
        });
    }

    public void updateWaterStatus(boolean valveOpen, boolean leak) {
        Platform.runLater(() -> {
            if (valveStatusLabel != null) {
                valveStatusLabel.setText(valveOpen ? "VALVE: OPEN" : "VALVE: CLOSED");
                valveStatusLabel.setStyle(valveOpen ? "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;" : "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f44336;");

                leakAlertLabel.setText(leak ? "CRITICAL: LEAK DETECTED!" : "NO LEAKS DETECTED");
                leakAlertLabel.setStyle(leak ? "-fx-font-size: 14px; -fx-text-fill: red; -fx-font-weight: bold;" : "-fx-font-size: 14px; -fx-text-fill: #9e9e9e;");
            }
        });
    }








    // --- Window ---

    @FXML
    private void onOpenWindow() {
        sendEvent(new Event("OPEN", "User opened window", "LIVING_ROOM_WINDOW"));
    }

    @FXML
    private void onCloseWindow() {
        sendEvent(new Event("CLOSE", "User closed window", "LIVING_ROOM_WINDOW"));
    }

    @FXML
    private void onShadeUp() {
        sendEvent(new Event("SHADE_UP", "User raised shade", "LIVING_ROOM_WINDOW"));
    }

    @FXML
    private void onShadeDown() {
        sendEvent(new Event("SHADE_DOWN", "User lowered shade", "LIVING_ROOM_WINDOW"));
    }

    @FXML
    private void onSetShade() {
        int level = (int) shadeSlider.getValue();
        System.out.println("Check level: " + level);
        sendEvent(new Event("SHADE_SET", String.valueOf(level), "LIVING_ROOM_WINDOW"));
    }

    // --- Door ---

    @FXML
    private void onOpenDoor() {
        sendEvent(new Event("OPEN", "User opened door", "FRONT_DOOR"));
    }

    @FXML
    private void onCloseDoor() {
        sendEvent(new Event("CLOSE", "User closed door", "FRONT_DOOR"));
    }

    @FXML
    private void onLockDoor() {
        sendEvent(new Event("LOCK", "User locked door", "FRONT_DOOR"));
    }

    @FXML
    private void onUnlockDoor() {
        sendEvent(new Event("UNLOCK", "User unlocked door", "FRONT_DOOR"));
    }

    private void sendEvent(Event event) {
        if (eventPool != null) {
            try {
                eventPool.put(event);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // --- Callbacks for Background Reactors (Thread-Safe UI Updates) ---

    public void updateLightStatus(boolean isOn) {
        Platform.runLater(() -> {
            lightIcon.setImage(isOn ? imgLightOn : imgLightOff);
            lightStatus.setText(isOn ? "Status: ON" : "Status: OFF");
        });
    }

    public void updateFanStatus(boolean isOn) {
        Platform.runLater(() -> {
            // If isOn is true, JavaFX will automatically animate the .gif image!
            fanIcon.setImage(isOn ? imgFanOn : imgFanOff);
            fanStatus.setText(isOn ? "Status: RUNNING" : "Status: OFF");
        });
    }


    public void updateThermostatStatus(double currentTemp, double targetTemp, String mode) {
        Platform.runLater(() -> {

            // tempLabel.setText(currentTemp + "°C / Target: " + targetTemp + "°C");
            // modeLabel.setText("Mode: " + mode);
            System.out.println(String.format("UI UPDATE: Temp %.1f°C (Target: %.1f°C) [%s]", currentTemp, targetTemp, mode));
        });
    }



    public void updateWindowStatus(boolean isOpen, int shadeLevel) {
        Platform.runLater(() -> {
            if (imgWindowOpen != null && imgWindowClosed != null) {
                windowIcon.setImage(isOpen ? imgWindowOpen : imgWindowClosed);
//                windowIcon.setOpacity(1.0 - (shadeLevel / 100.0) * 0.7);
            }
            windowStatus.setText((isOpen ? "OPEN" : "CLOSED") + " | Shade: " + shadeLevel + "%");
        });
    }

    public void updateDoorStatus(boolean isOpen, boolean isLocked) {
        Platform.runLater(() -> {
            doorIcon.setImage(isOpen ? imgDoorOpen : (isLocked ? imgDoorLocked : imgDoorClosed));
            doorStatus.setText(isOpen ? "OPEN" : "CLOSED" + (isLocked ? " | LOCKED" : " | UNLOCKED"));
        });
    }





}