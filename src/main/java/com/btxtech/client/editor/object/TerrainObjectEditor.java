package com.btxtech.client.editor.object;

import com.btxtech.client.TerrainMouseDownEvent;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.SlopeNameId;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

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
    private Logger logger = Logger.getLogger(TerrainObjectEditor.class.getName());
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainObjectService terrainObjectService;
    private SlopeNameId newObjectId;
    private double randomZRotation = 1.0;
    private double randomScale = 2.0;
    private Collection<TerrainObjectPosition> terrainObjects;
    private boolean active;

    public void onTerrainMouseDown(@Observes TerrainMouseDownEvent terrainMouseDownEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseDownEvent.getWorldPickRay();
            Vertex terrainPosition = terrainSurface.calculatePositionOnTerrain(ray3d);
            // Create new terrain object position
            TerrainObjectPosition objectPosition = new TerrainObjectPosition();
            if (randomScale < 1.0) {
                throw new IllegalArgumentException("randomScale < 1.0: " + randomScale);
            }
            objectPosition.setScale(1.0 / randomScale + (randomScale - 1.0 / randomScale) * Math.random());
            logger.severe("Scale: " + objectPosition.getScale());
            objectPosition.setZRotation(MathHelper.ONE_RADIANT * Math.random());
            objectPosition.setPosition(terrainPosition);
            objectPosition.setTerrainObjectId(newObjectId.getId());
            terrainObjects.add(objectPosition);
            terrainObjectService.setupModelMatrices(terrainObjects);
            renderService.updateObjectModelMatrices();
        }
    }

    public void setNewObjectId(SlopeNameId newObjectId) {
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
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
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
