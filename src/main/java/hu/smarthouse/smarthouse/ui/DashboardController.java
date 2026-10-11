package hu.smarthouse.smarthouse.ui;

import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class DashboardController {

    @FXML private TextArea logArea;

    // Light Components
    @FXML private ImageView lightIcon;
    @FXML private Label lightStatus;

    // Fan Components
    @FXML private ImageView fanIcon;
    @FXML private Label fanStatus;

    // Settings Components
    @FXML private Slider fanDelaySlider;

    private EventPool eventPool;

    // Image assets (Ensure these files are in src/main/resources/assets/)
    private Image imgLightOn;
    private Image imgLightOff;
    private Image imgFanOn; // This should be your .gif file
    private Image imgFanOff; // Static .png file

    @FXML
    public void initialize() {
        // Load images on startup. Try-catch blocks ensure the app doesn't crash if images are missing yet.
        try {
            imgLightOn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/light_on.png")));
            imgLightOff = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/light_off.png")));
            imgFanOn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/fan_on.gif")));
            imgFanOff = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/hu/smarthouse/smarthouse/assets/fan_off.png")));

            // Set initial off states
            lightIcon.setImage(imgLightOff);
            fanIcon.setImage(imgFanOff);
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
}