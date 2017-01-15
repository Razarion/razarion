package com.btxtech.client;


import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.TerrainEditor;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import elemental.client.Browser;
import elemental.events.Event;
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
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private SelectionHandler selectionHandler;
    private Set<Integer> keysDown = new HashSet<>();
    private TerrainEditor terrainEditor;

    public void init() {
        Browser.getWindow().addEventListener(Event.KEYDOWN, evt -> {
            try {
                KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                switch (keyboardEvent.getKeyCode()) {
                    case 65:
                    case KeyCodes.KEY_LEFT: {
                        terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.LEFT, null);
                        break;
                    }
                    case 68:
                    case KeyCodes.KEY_RIGHT: {
                        terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.RIGHT, null);
                        break;
                    }
                    case 87:
                    case KeyCodes.KEY_UP: {
                        terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.TOP);
                        break;
                    }
                    case 83:
                    case KeyCodes.KEY_DOWN: {
                        terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.BOTTOM);
                        break;
                    }
                    case KeyCodes.KEY_ESCAPE: {
                        selectionHandler.clearSelection(false);
                        break;
                    }
                }
                if (!keysDown.contains(keyboardEvent.getKeyCode())) {
                    keysDown.add(keyboardEvent.getKeyCode());
                    if (terrainEditor != null && keyboardEvent.getKeyCode() == KeyboardEvent.KeyCode.DELETE) {
                        terrainEditor.onDeleteKeyDown(true);
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Handling key down events failed: " + evt, t);
            }
        }, true);
        Browser.getWindow().addEventListener(Event.KEYUP, evt -> {
            try {
                KeyboardEvent keyboardEvent = (KeyboardEvent) evt;
                switch (keyboardEvent.getKeyCode()) {
                    case 65:
                    case KeyCodes.KEY_LEFT: {
                        terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.STOP, null);
                        break;
                    }
                    case 68:
                    case KeyCodes.KEY_RIGHT: {
                        terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.STOP, null);
                        break;
                    }
                    case 87:
                    case KeyCodes.KEY_UP: {
                        terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.STOP);
                        break;
                    }
                    case 83:
                    case KeyCodes.KEY_DOWN: {
                        terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.STOP);
                        break;
                    }
                }
                keysDown.remove(keyboardEvent.getKeyCode());
                if (terrainEditor != null && keyboardEvent.getKeyCode() == KeyboardEvent.KeyCode.DELETE) {
                    terrainEditor.onDeleteKeyDown(false);
                }

            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Handling key up events failed: " + evt, t);
            }
        }, true);
    }

    public void setTerrainEditor(TerrainEditor terrainEditor) {
        this.terrainEditor = terrainEditor;
    }
}
