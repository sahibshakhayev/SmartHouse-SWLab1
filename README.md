# Centralized, Event-Driven Smart House Prototype

A multi-threaded smart-home simulation and control center built in **Java 25+** and **JavaFX** for **Software Lab 1** at **Eötvös Loránd University (ELTE), Faculty of Informatics**.

The system coordinates **9 smart devices across 4 rooms** alongside shared electrical and water infrastructure using a centralized producer–consumer **Reactor architecture**, deterministic **4-tier priority routing**, **hardware-style safety interlocks**, and **pre-admission electrical capacity protection**.

* **Team Members:** Sahib Shakhayev, Asel Akylbek Kyzy
* **Supervisor:** Prof. Dr. Viktória Zsók

---

## Architecture Overview

Rather than allowing sensors and appliances to call one another directly—which causes race conditions, relay oscillation, and conflicting automation—all components communicate asynchronously through a centralized event pipeline:

```text
[ JavaFX UI / Motion & Leak Sensors / OpenWeatherMap API ]
                           │  emits immutable Event (v=1;key=value;ts=...)
                           ▼
              [ EventPool (PriorityBlockingQueue) ]
                           │  orders: EMERGENCY > USER_OVERRIDE > AUTOMATION > BACKGROUND
                           ▼
                    [ MainReactor ] ──► [ InterlockRules & CommandGate (30s hold) ]
                           │
            ┌──────────────┴──────────────┐
            ▼                             ▼
[ ConsequencePool (ConcurrentHashMap) ]  [ monitorFeed -> SystemMonitor ]
            │                             │  Platform.runLater()
            ▼                             ▼
[ Dedicated Unit Reactors & Actuators ]  [ JavaFX Dashboard & Live Log ]
```

### Core Engineering Mechanisms
* **Decoupled Domain & Concurrency Layers:** Pure domain `Actuator` classes encapsulate state transitions, bounds validation (`NumbersUtil`), and wattage ratings without thread dependencies, while asynchronous `BaseReactor` workers consume from per-unit `ConsequencePool` queues.
* **4-Tier Priority Routing (`EventPool`):** Processes events strictly in priority order (`EMERGENCY` > `USER_OVERRIDE` > `AUTOMATION` > `BACKGROUND`, FIFO within equal tiers).
* **Manual Override Protection (`CommandGate`):** Any manual `USER_OVERRIDE` interaction registers a **30-second hold** on that unit, blocking conflicting `AUTOMATION` triggers (`GATE_BLOCKED`) while still permitting `EMERGENCY` overrides.
* **Emergency Interlocks (`InterlockRules`):** A water leak (`LEAK_DETECTED`) closes the master water valve, cuts wet-zone electrical feeders, and locks the Smart Water Boiler (`OFF`) against dry-run heating until `RESOLVE_LEAK` is explicitly issued.
* **Pre-Admission Capacity Control (`PowerBreakerReactor` & `ShedPlanner`):** Tracks per-device load reports (`UPDATE_DEVICE_LOAD` formatted as `LINE:DEVICE:kW`) against a **5.00 kW** main breaker limit. Any device activation that would exceed `5.00 kW` is immediately reverted via an `EMERGENCY` `TURN_OFF` command with a diagnostic alert banner (`CAPACITY_EXCEEDED`).
* **Global Scene Orchestration (`HouseScenes`):** Coordinated presets (`HOME`, `AWAY`, `NIGHT`, `ECO`) sequence power-line restoration with stabilization pauses before configuring climate, lighting, security, and entertainment devices.

---

## Smart House Topology (4 Rooms & 9 Units)

| Room / Circuit | Smart Units & Infrastructure | Key Capabilities & Ratings |
| :--- | :--- | :--- |
| **Living Room** (`LIVING_ROOM_LINE`) | Smart Light, Smart Thermostat, Motorized Window Shades, Entertainment Center (TV & Music System) | Thermostat bounds (`15°C–30°C`), `<20.0°C` heating recovery (`1.50 kW`), sunset shade closure, Smart TV (`0.15 kW`), Music System (`0.08 kW`) |
| **Kitchen** (`KITCHEN_LINE`) | Smart Light, Water Leak Sensor, Master Water Valve, High-Load Smart Plug | Instant master valve shutoff & wet-zone feeder isolation on leak detection, `1.00 kW` plug load reporting |
| **Bathroom** (`BATHROOM_LINE`) | Smart Light, Motion Sensor, Ventilation Fan, Smart Water Boiler, Smart Plug | Presence-triggered light/fan timers (`ScheduledExecutorService`), Boiler modes (`OFF`, `ECO` `1.10 kW`, `BOOST` `2.20 kW`) with dry-run lockout |
| **Corridor & Grid** (`CORRIDOR_LINE`) | Smart Entry Door, Motion Sensor, Main Electrical Breaker Panel | Entry lock state (`LOCKED` / `UNLOCKED`), 4-line power distribution board with `5.00 kW` capacity ceiling |

---

## Prerequisites & Local Setup

1. **Java Development Kit (JDK) 25 or higher** (configured with Java Platform Module System support).
2. **Maven Wrapper** (bundled in the repository—no separate Maven installation required):
    * Windows: `.\mvnw.cmd`
    * Linux / macOS: `./mvnw`
3. **OpenWeatherMap API Key (Optional for live outdoor temperature):**
   Create a `.env` file in the project root directory (loaded securely via `dotenv-java`):
   ```env
   OPENWEATHER_API_KEY=your_openweathermap_api_key_here
   ```
   *(If omitted, the application still runs normally using simulated ambient weather.)*

---

## Running Tests & Launching the Application

Clone the repository and navigate into the project root:

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd <YOUR_REPOSITORY_FOLDER>
```

### 1. Run Unit & 50-Thread Concurrency Stress Tests
Executes the JUnit 5 test suites (`SystemStressTest`, `CommandGateTest`, `ShedPlannerTest`) validating 50 concurrent threads, 1,000 simultaneous events with zero message loss, priority ordering, manual override holds, and emergency interlocks:

* **Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd test
  ```
* **Linux / macOS:**
  ```bash
  ./mvnw test
  ```

### 2. Launch the JavaFX Smart House Control Center
Starts the single-window MVC dashboard with live room controls, global scene triggers, and the real-time `System Monitor Log`:

* **Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd javafx:run
  ```
* **Linux / macOS:**
  ```bash
  ./mvnw javafx:run
  ```