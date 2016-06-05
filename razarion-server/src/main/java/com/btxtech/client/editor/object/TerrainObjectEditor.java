package com.btxtech.client.editor.object;

import com.btxtech.client.TerrainKeyDownEvent;
import com.btxtech.client.TerrainKeyUpEvent;
import com.btxtech.client.TerrainMouseDownEvent;
import com.btxtech.client.TerrainMouseMoveEvent;
import com.btxtech.client.TerrainMouseUpEvent;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;
import elemental.events.KeyboardEvent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.05.2016.
 */
@Singleton
public class TerrainObjectEditor {
    public enum CursorType {
        NORMAL,
        HOVER,
        DELETE_MODE,
        SELECTED,
        DELETE_SELECTED
    }

    private Logger logger = Logger.getLogger(TerrainObjectEditor.class.getName());
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private Event<TerrainObjectEditorSelectedEvent> terrainObjectEditorSelectedEvent;
    private ObjectNameId newObjectId;
    private double randomZRotation = 1.0;
    private double randomScale = 2.0;
    private Collection<TerrainObjectPosition> terrainObjects;
    private boolean active;
    private TerrainObjectPosition selected;
    private boolean deletePressed;
    private boolean hover;

    public void onTerrainMouseMove(@Observes TerrainMouseMoveEvent terrainMouseMoveEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseMoveEvent.getWorldPickRay();
            Vertex terrainPosition = terrainSurface.calculatePositionOnZeroLevel(ray3d);

            CursorType cursorType;
            hover = false;
            if (selected != null) {
                selected.setPosition(terrainPosition);
                terrainObjectService.setupModelMatrices(terrainObjects);
                cursorType = CursorType.SELECTED;
            } else if (getAtTerrain(terrainPosition) != null) {
                hover = true;
                if (deletePressed) {
                    cursorType = CursorType.DELETE_SELECTED;
                } else {
                    cursorType = CursorType.HOVER;
                }
            } else {
                if (deletePressed) {
                    cursorType = CursorType.DELETE_MODE;
                } else {
                    cursorType = CursorType.NORMAL;
                }
            }
            terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(cursorType, terrainPosition));
        }
    }


    public void onTerrainMouseDown(@Observes TerrainMouseDownEvent terrainMouseDownEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseDownEvent.getWorldPickRay();
            Vertex terrainPosition = terrainSurface.calculatePositionOnZeroLevel(ray3d);

            selected = getAtTerrain(terrainPosition);

            if (selected == null && !deletePressed) {
                // Create new terrain object position
                TerrainObjectPosition objectPosition = new TerrainObjectPosition();
                if (randomScale < 1.0) {
                    throw new IllegalArgumentException("randomScale < 1.0: " + randomScale);
                }
                objectPosition.setScale(1.0 / randomScale + (randomScale - 1.0 / randomScale) * Math.random());
                objectPosition.setZRotation(MathHelper.ONE_RADIANT * Math.random());
                objectPosition.setPosition(terrainPosition);
                objectPosition.setTerrainObjectId(newObjectId.getId());
                terrainObjects.add(objectPosition);
                selected = objectPosition;
                terrainObjectService.setupModelMatrices(terrainObjects);
                terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.SELECTED, terrainPosition));
            } else if (selected != null && deletePressed) {
                deleteSelected();
            }

            // terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(terrainPosition));
        }
    }

    private void deleteSelected() {
        terrainObjects.remove(selected);
        selected = null;
        terrainObjectService.setupModelMatrices(terrainObjects);
        terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.NORMAL, null));
    }

    public void onTerrainMouseUp(@Observes TerrainMouseUpEvent terrainMouseDownEvent) {
        Ray3d ray3d = terrainMouseDownEvent.getWorldPickRay();
        Vertex terrainPosition = terrainSurface.calculatePositionOnZeroLevel(ray3d);

        if (selected != null) {
            selected = null;
            terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.HOVER, terrainPosition));
        }
    }


    public void onTerrainKeyDown(@Observes TerrainKeyDownEvent terrainKeyDownEvent) {
        if (terrainKeyDownEvent.getKeyboardEvent().getKeyCode() == KeyboardEvent.KeyCode.DELETE) {
            deletePressed = true;
            if (selected != null) {
                deleteSelected();
            } else if (hover) {
                terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.DELETE_SELECTED, null));
            } else {
                terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.DELETE_MODE, null));
            }
        }
    }

    public void onTerrainKeyUp(@Observes TerrainKeyUpEvent terrainKeyUpEvent) {
        if (terrainKeyUpEvent.getKeyboardEvent().getKeyCode() == KeyboardEvent.KeyCode.DELETE) {
            deletePressed = false;
            if (selected != null) {
                terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.SELECTED, null));
            } else if (hover) {
                terrainObjectEditorSelectedEvent.fire(new TerrainObjectEditorSelectedEvent(CursorType.HOVER, null));
            }
        }

    }

    private TerrainObjectPosition getAtTerrain(Vertex terrainPosition) {
        for (TerrainObjectPosition terrainObject : terrainObjects) {
            if (terrainObject.getPosition().toXY().getDistance(terrainPosition.toXY()) < 10) {
                return terrainObject;
            }
        }
        return null;
    }

    public void setNewObjectId(ObjectNameId newObjectId) {
        this.newObjectId = newObjectId;
    }

    public double getRandomZRotation() {
        return randomZRotation;
    }

    public void setRandomZRotation(double randomZRotation) {
        this.randomZRotation = randomZRotation;
    }

    public double getRandomScale() {
        return randomScale;
    }

    public void setRandomScale(double randomScale) {
        this.randomScale = randomScale;
    }

    public void setTerrainObjects(Collection<TerrainObjectPosition> terrainObjectPositions) {
        terrainObjects = new ArrayList<>(terrainObjectPositions);
    }

    public void activate() {
        if (active) {
            return;
        }
        active = true;
        renderService.setShowObjectEditor(true);
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        selected = null;
        renderService.setShowObjectEditor(false);
    }


    public void save() {
        terrainEditorService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void ignore) {
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveTerrainObjectPositions failed: " + message, throwable);
                return false;
            }
        }).saveTerrainObjectPositions(terrainObjects);
    }

}
