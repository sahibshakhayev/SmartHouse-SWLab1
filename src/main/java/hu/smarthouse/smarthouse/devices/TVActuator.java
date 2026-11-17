package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.model.Actuator;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.utils.NumbersUtil;

public class TVActuator extends Actuator {
    public static final double POWER_KW = 0.15;
    private static final int MAX_CHANNEL = 99;

    private boolean on = false;
    private int channel = 1;
    private int volume = 20;

    public TVActuator(String compID) { super(compID); }

    @Override
    protected synchronized void performAction(Event event) {
        switch (event.getType()) {
            case "TURN_ON" -> on = true;
            case "TURN_OFF" -> on = false;
            case "TOGGLE" -> on = !on;
            case "CHANNEL_UP" -> { requireOn(); channel = channel % MAX_CHANNEL + 1; }
            case "CHANNEL_DOWN" -> { requireOn(); channel = channel == 1 ? MAX_CHANNEL : channel - 1; }
            case "CHANNEL_SET" -> { requireOn(); channel = NumbersUtil.clamp(NumbersUtil.parse(event.getMessage()), 1, MAX_CHANNEL); }
            case "VOLUME_UP" -> { requireOn(); volume = NumbersUtil.clamp(volume + 5, 0, 100); }
            case "VOLUME_DOWN" -> { requireOn(); volume = NumbersUtil.clamp(volume - 5, 0, 100); }
            case "VOLUME_SET" -> { requireOn(); volume = NumbersUtil.clamp(NumbersUtil.parse(event.getMessage()), 0, 100); }
            default -> throw new IllegalArgumentException("TV does not support event type: " + event.getType());
        }
    }

    private void requireOn() {
        if (!on) throw new IllegalStateException("TV is off");
    }

    public synchronized boolean isOn() { return on; }
    public synchronized int getChannel() { return channel; }
    public synchronized int getVolume() { return volume; }
    public synchronized double getPowerDrawKw() { return on ? POWER_KW : 0.0; }
}