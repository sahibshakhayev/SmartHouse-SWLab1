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

    // Fan Components
    @FXML private ImageView fanIcon;
    @FXML private Label fanStatus;

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
    private void onShowBathroom() {
        showView(true);
    }

    @FXML
    private void onShowLivingRoom() {
        showView(false);
    }

    private void showView(boolean bathroom) {
        bathroomView.setVisible(bathroom);
        bathroomView.setManaged(bathroom);
        livingRoomView.setVisible(!bathroom);
        livingRoomView.setManaged(!bathroom);

        pageTitle.setText(bathroom ? "Bathroom Dashboard" : "Living Room Dashboard");
        bathroomNavButton.setStyle(bathroom ? NAV_ACTIVE : NAV_INACTIVE);
        livingRoomNavButton.setStyle(bathroom ? NAV_INACTIVE : NAV_ACTIVE);
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