package hu.smarthouse.smarthouse.core;
import hu.smarthouse.smarthouse.model.Event;



public interface Reactor extends Runnable {


    void react(Event event);

    String getComponentId();
}

