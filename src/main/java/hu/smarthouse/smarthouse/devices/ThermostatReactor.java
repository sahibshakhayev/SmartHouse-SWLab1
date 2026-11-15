package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.core.Priority;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThermostatReactor extends BaseReactor {
    private static final String ID = "LIVING_ROOM_THERMOSTAT";
    private static final String LINE = "LIVING_ROOM_LINE";

    private final ThermostatActuator thermostat = new ThermostatActuator(ID);
    private final DashboardController controller;
    private final EventPool eventPool;
    private final ScheduledExecutorService backgroundTasks = Executors.newScheduledThreadPool(2);
    private final HttpClient httpClient;

    private final String apiKey;
    private static final String CITY = "Budapest";

    public ThermostatReactor(ConsequencePool pool, DashboardController controller, EventPool eventPool) {
        super(ID, pool);
        this.controller = controller;
        this.eventPool = eventPool;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.apiKey = dotenv.get("OPENWEATHER_API_KEY");

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("[ThermostatReactor] ERROR: OPENWEATHER_API_KEY not found in .env!");
        }

        startEnvironmentSimulation();
        startWeatherPolling();
    }

    @Override
    public void react(Event event) {
        try {
            if ("LOW_TEMPERATURE".equals(event.getType())) {
                if ("HEATING".equals(thermostat.getMode())) {
                    thermostat.execute(new Event("RESTORE_DEFAULT_TEMP", "Emergency heating triggered", ID));
                } else {
                    System.out.println("[Thermostat] LOW_TEMPERATURE ignored: System is not in HEATING mode.");
                }
            } else {
                thermostat.execute(event);
            }

            publishDeviceLoad();
            updateUI();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Thermostat] rejected: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void publishDeviceLoad() throws InterruptedException {
        double loadKw = "HEATING".equals(thermostat.getMode()) ? 1.5 : 0.0;
        eventPool.put(new Event("UPDATE_DEVICE_LOAD", LINE + ":" + ID + ":" + loadKw, "MAIN_POWER_BREAKER"));
    }

    private void startEnvironmentSimulation() {
        backgroundTasks.scheduleAtFixedRate(() -> {
            try {
                boolean changed = thermostat.simulateAmbientFluctuation();

                if (thermostat.getCurrentTemp() < 20.0
                        && thermostat.getTargetTemp() < 20.0
                        && "HEATING".equals(thermostat.getMode())) {
                    eventPool.put(new Event("LOW_TEMPERATURE", "Temperature dropped below 20C threshold", ID));
                }

                if (thermostat.isAutoMode()) {
                    String desiredMode = "OFF";
                    if (thermostat.getCurrentTemp() < thermostat.getTargetTemp() - 0.5) {
                        desiredMode = "HEATING";
                    } else if (thermostat.getCurrentTemp() > thermostat.getTargetTemp() + 0.5) {
                        desiredMode = "COOLING";
                    }

                    if (!thermostat.getMode().equals(desiredMode)) {
                        thermostat.execute(new Event("INTERNAL_SET_MODE", desiredMode, ID));
                        publishDeviceLoad();
                        changed = true;
                    }
                }

                if (changed) {
                    updateUI();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void startWeatherPolling() {
        backgroundTasks.scheduleAtFixedRate(() -> {
            if (apiKey == null || apiKey.isEmpty()) return;

            try {
                String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + apiKey;
                System.out.println("[Weather API] Fetching current outdoor temperature for " + CITY + "...");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(weatherApiUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject json = new JSONObject(response.body());
                    double realTemp = json.getJSONObject("main").getDouble("temp");
                    long currentTime = json.getLong("dt");
                    long sunsetTime = json.getJSONObject("sys").getLong("sunset");

                    if (currentTime >= sunsetTime) {
                        eventPool.put(new Event("DARKNESS_DETECTED", "Sun has set according to API", "LIVING_ROOM_WINDOW"));
                    }

                    String weatherMsg = String.format("Outdoor temp updated to %.1f°C", realTemp);
                    System.out.println("[Weather API] " + weatherMsg);
                    eventPool.put(new Event("WEATHER_UPDATE", weatherMsg, "SYSTEM_MONITOR", Priority.BACKGROUND));

                    thermostat.execute(new Event("OUTDOOR_TEMP_UPDATE", String.valueOf(realTemp), ID));
                    updateUI();
                } else {
                    System.err.println("[Weather API] Failed with status code: " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("[Weather API] Connection error: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    private void updateUI() {
        controller.updateThermostatStatus(
                thermostat.getCurrentTemp(),
                thermostat.getTargetTemp(),
                thermostat.getOutdoorTemp(),
                thermostat.getMode(),
                thermostat.isAutoMode()
        );
    }

    @Override
    public void stop() {
        super.stop();
        backgroundTasks.shutdownNow();
    }
}