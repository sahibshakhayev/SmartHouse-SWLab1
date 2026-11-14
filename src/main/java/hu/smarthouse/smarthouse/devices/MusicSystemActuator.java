package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.utils.NumbersUtil;

import java.util.List;

public class MusicSystemActuator extends Actuator {
    public static final double POWER_KW = 0.08;
    public static final List<String> TRACKS =
            List.of("Morning Jazz", "Chill Beats", "Classical Piano", "Sleep Waves", "Rock Classics");

    private boolean on = false;
    private int track = 0;
    private int volume = 25;

    public MusicSystemActuator(String compID) { super(compID); }

    @Override
    protected synchronized void performAction(Event event) {
        switch (event.getType()) {
            case "TURN_ON" -> on = true;
            case "TURN_OFF" -> on = false;
            case "TOGGLE" -> on = !on;
            case "NEXT_TRACK" -> { requireOn(); track = (track + 1) % TRACKS.size(); }
            case "PREV_TRACK" -> { requireOn(); track = (track - 1 + TRACKS.size()) % TRACKS.size(); }
            case "TRACK_SET" -> { requireOn(); track = NumbersUtil.clamp(NumbersUtil.parse(event.getMessage()), 1, TRACKS.size()) - 1; } // 1-based
            case "VOLUME_UP" -> { requireOn(); volume = NumbersUtil.clamp(volume + 5, 0, 100); }
            case "VOLUME_DOWN" -> { requireOn(); volume = NumbersUtil.clamp(volume - 5, 0, 100); }
            case "VOLUME_SET" -> { requireOn(); volume = NumbersUtil.clamp(NumbersUtil.parse(event.getMessage()), 0, 100); }
            default -> throw new IllegalArgumentException("Music system does not support event type: " + event.getType());
        }
    }

    private void requireOn() {
        if (!on) throw new IllegalStateException("Music system is off");
    }

    public synchronized boolean isOn() { return on; }
    public synchronized String getTrackName() { return TRACKS.get(track); }
    public synchronized int getVolume() { return volume; }
    public synchronized double getPowerDrawKw() { return on ? POWER_KW : 0.0; }
}