package hu.smarthouse.smarthouse.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class Protocol {
    private Protocol() {
    }

    public static final String VERSION = "1";

    public static final String LIGHT_STATUS = "LIGHT_STATUS";
    public static final String PLUG_STATUS = "PLUG_STATUS";
    public static final String MOTION_DETECTED = "MOTION_DETECTED";
    public static final String MOTION_CLEARED = "MOTION_CLEARED";

    private static final Set<String> TELEMETRY =
            Set.of(LIGHT_STATUS, PLUG_STATUS, MOTION_DETECTED, MOTION_CLEARED);

    public static boolean isTelemetry(String type) {
        return TELEMETRY.contains(type);
    }

    /**
     * encode("state","ON","watts","60") -> "v=1;state=ON;watts=60;ts=1730000000000"
     */
    public static String encode(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("encode needs key/value pairs");
        }
        StringBuilder sb = new StringBuilder("v=").append(VERSION);
        for (int i = 0; i < keyValues.length; i += 2) {
            sb.append(';').append(keyValues[i]).append('=').append(keyValues[i + 1]);
        }
        return sb.append(";ts=").append(System.currentTimeMillis()).toString();
    }

    public static Map<String, String> decode(String message) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String part : message.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                throw new IllegalArgumentException("Malformed field: " + part);
            }
            fields.put(part.substring(0, eq), part.substring(eq + 1));
        }
        if (!VERSION.equals(fields.get("v"))) {
            throw new IllegalArgumentException("Unsupported protocol version: " + fields.get("v"));
        }
        return fields;
    }
}