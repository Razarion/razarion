package com.btxtech.client;


import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.05.2016.
 */
@Singleton
public class KeyboardEventHandler {
    private Logger logger = Logger.getLogger(KeyboardEventHandler.class.getName());
    @Inject
    private javax.enterprise.event.Event<TerrainKeyDownEvent> terrainKeyDownEvent;
    @Inject
    private javax.enterprise.event.Event<TerrainKeyUpEvent> terrainKeyUpEventEvent;
    private Set<Integer> keysDown = new HashSet<>();

    public void init() {
        Browser.getWindow().addEventListener(Event.KEYDOWN, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                try {
                    KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                    if(keysDown.contains(keyboardEvent.getKeyCode())) {
                        return;
                    }
                    keysDown.add(keyboardEvent.getKeyCode());
                    terrainKeyDownEvent.fire(new TerrainKeyDownEvent(keyboardEvent));
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Handling key down events failed: " + evt, t);
                }
            }
        }, false);
        Browser.getWindow().addEventListener(Event.KEYUP, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                try {
                    KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                    keysDown.remove(keyboardEvent.getKeyCode());
                    terrainKeyUpEventEvent.fire(new TerrainKeyUpEvent(keyboardEvent));
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Handling key up events failed: " + evt, t);
                }
            }
        }, false);
    }
}
