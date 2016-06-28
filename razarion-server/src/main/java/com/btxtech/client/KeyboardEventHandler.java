package com.btxtech.client;


import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.google.gwt.event.dom.client.KeyCodes;
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
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    private Set<Integer> keysDown = new HashSet<>();

    public void init() {
        Browser.getWindow().addEventListener(Event.KEYDOWN, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                try {
                    KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                    switch (keyboardEvent.getKeyCode()) {
                        case 65:
                        case KeyCodes.KEY_LEFT: {
                            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.LEFT, null);
                            // TODO evt.cancel(); // Prevent from scrolling the browser window
                            break;
                        }
                        case 68:
                        case KeyCodes.KEY_RIGHT: {
                            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.RIGHT, null);
                            // TODO evt.cancel();
                            break;
                        }
                        case 87:
                        case KeyCodes.KEY_UP: {
                            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.TOP);
                            // TODO evt.cancel();
                            break;
                        }
                        case 83:
                        case KeyCodes.KEY_DOWN: {
                            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.BOTTOM);
                            // TODO  evt.cancel();
                            break;
                        }
                        case KeyCodes.KEY_ESCAPE: {
                            // TODO  CockpitMode.getInstance().onEscape();
                            break;
                        }
                    }

                    if(!keysDown.contains(keyboardEvent.getKeyCode())) {
                        keysDown.add(keyboardEvent.getKeyCode());
                        terrainKeyDownEvent.fire(new TerrainKeyDownEvent(keyboardEvent));
                    }
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Handling key down events failed: " + evt, t);
                }
            }
        }, true);
        Browser.getWindow().addEventListener(Event.KEYUP, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                try {
                    KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                    switch (keyboardEvent.getKeyCode()) {
                        case 65:
                        case KeyCodes.KEY_LEFT: {
                            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.STOP, null);
                            // TODO evt.cancel(); // Prevent from scrolling the browser window
                            break;
                        }
                        case 68:
                        case KeyCodes.KEY_RIGHT: {
                            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.STOP, null);
                            // TODO evt.cancel();
                            break;
                        }
                        case 87:
                        case KeyCodes.KEY_UP: {
                            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.STOP);
                            // TODO evt.cancel();
                            break;
                        }
                        case 83:
                        case KeyCodes.KEY_DOWN: {
                            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.STOP);
                            // TODO evt.cancel();
                            break;
                        }
                    }
                    keysDown.remove(keyboardEvent.getKeyCode());
                    terrainKeyUpEventEvent.fire(new TerrainKeyUpEvent(keyboardEvent));
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Handling key up events failed: " + evt, t);
                }
            }
        }, true);
    }
}
