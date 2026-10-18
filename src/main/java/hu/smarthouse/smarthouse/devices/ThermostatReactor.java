package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;
import org.json.JSONObject;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThermostatReactor extends BaseReactor {
    private final ThermostatActuator thermostat = new ThermostatActuator("LIVING_ROOM_THERMOSTAT");
    private final DashboardController controller;
    private final ScheduledExecutorService backgroundTasks = Executors.newScheduledThreadPool(2);
    private final HttpClient httpClient;

    private final String apiKey;
    private static final String CITY = "Budapest";
    public ThermostatReactor(ConsequencePool pool, DashboardController controller) {
        super("LIVING_ROOM_THERMOSTAT", pool);
        this.controller = controller;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Безопасная загрузка ключа из .env файла
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.apiKey = dotenv.get("OPENWEATHER_API_KEY");

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("[ThermostatReactor] ОШИБКА: OPENWEATHER_API_KEY не найден в файле .env!");
        }

        startEnvironmentSimulation();
        startWeatherPolling();
    }

    @Override
    public void react(Event event) {
        try {
            thermostat.execute(event);
            updateUI();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Thermostat] rejected: " + e.getMessage());
        }
    }

    private void startEnvironmentSimulation() {
        // Fast loop: runs every 2 seconds for smooth UI simulation
        backgroundTasks.scheduleAtFixedRate(() -> {
            boolean changed = thermostat.simulateAmbientFluctuation();

            if (thermostat.isAutoMode()) {
                String desiredMode = "OFF";
                if (thermostat.getCurrentTemp() < thermostat.getTargetTemp() - 0.5) {
                    desiredMode = "HEATING";
                } else if (thermostat.getCurrentTemp() > thermostat.getTargetTemp() + 0.5) {
                    desiredMode = "COOLING";
                }

                if (!thermostat.getMode().equals(desiredMode)) {
                    thermostat.execute(new Event("INTERNAL_SET_MODE", desiredMode, "LIVING_ROOM_THERMOSTAT"));
                    changed = true;
                }
            }

            if (changed) {
                updateUI();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void startWeatherPolling() {
        // Slow loop: runs immediately (0 delay), then every 10 minutes
        backgroundTasks.scheduleAtFixedRate(() -> {
            if (apiKey == null || apiKey.isEmpty()) return; // Защита от пустых запросов

            try {
                // Динамически собираем URL с нашим ключом
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


                    String successLog = String.format("[LOG] Component: WEATHER_API | Type: FETCH_SUCCESS | Message: Outdoor temp updated to %.1f°C\n", realTemp);


                    System.out.print(successLog);


                    javafx.application.Platform.runLater(() -> {
                        controller.getLogArea().appendText(successLog);
                    });
                    thermostat.execute(new Event("OUTDOOR_TEMP_UPDATE", String.valueOf(realTemp), "LIVING_ROOM_THERMOSTAT"));
                    updateUI();
                } else {
                    System.err.println("[Weather API] Failed with status code: " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("[Weather API] Connection error: " + e.getMessage());
            }
        }, 0, 2, TimeUnit.MINUTES);
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