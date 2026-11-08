package hu.smarthouse.smarthouse.devices;

import hu.smarthouse.smarthouse.core.*;
import hu.smarthouse.smarthouse.model.Event;

import java.util.List;

public class SceneReactor extends BaseReactor {
    private final EventPool eventPool;

    public SceneReactor(ConsequencePool pool, EventPool eventPool) {
        super("HOUSE_SCENES", pool);
        this.eventPool = eventPool;
    }

    @Override
    public void react(Event event) {
        if (!"SCENE_SET".equals(event.getType())) return;

        String name = event.getMessage();
        List<HouseScenes.Step> steps = HouseScenes.get(name);
        if (steps == null) {
            System.out.println("[Scenes] unknown scene: " + name);
            return;
        }

        try {
            for (HouseScenes.Step s : steps) {
                // same tier as the request: a user-chosen scene overrides automation, never an emergency
                eventPool.put(new Event(s.type(),
                        s.message().isEmpty() ? "Scene " + name : s.message(),
                        s.compId(), event.getPriority()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}