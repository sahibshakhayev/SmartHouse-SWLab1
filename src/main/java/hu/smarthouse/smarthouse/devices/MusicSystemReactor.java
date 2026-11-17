package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.BaseReactor;
import hu.smarthouse.smarthouse.core.ConsequencePool;
import hu.smarthouse.smarthouse.core.EventPool;
import hu.smarthouse.smarthouse.model.Event;
import hu.smarthouse.smarthouse.ui.DashboardController;

public class MusicSystemReactor extends BaseReactor {
    private static final String ID = "LIVING_ROOM_MUSIC";
    private static final String LINE = "LIVING_ROOM_LINE";

    private final MusicSystemActuator music = new MusicSystemActuator(ID);
    private final EventPool eventPool;
    private final DashboardController controller;

    public MusicSystemReactor(ConsequencePool pool, EventPool eventPool, DashboardController controller) {
        super(ID, pool);
        this.eventPool = eventPool;
        this.controller = controller;
    }

    @Override
    public void react(Event event) {
        try {
            music.execute(event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[Music System] rejected: " + e.getMessage());
            return;
        }

        controller.updateMusicStatus(music.isOn(), music.getTrackName(), music.getVolume());

        try {
            eventPool.put(new Event("UPDATE_DEVICE_LOAD",
                    LINE + ":" + ID + ":" + music.getPowerDrawKw(), "MAIN_POWER_BREAKER"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}