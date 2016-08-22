package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.shared.Shape3DProvider;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.Shape3DUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 17.08.2016.
 */
@ApplicationScoped
public class Shape3DCrud {
    private Logger logger = Logger.getLogger(Shape3DCrud.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<Shape3DProvider> caller;
    @Inject
    private Shape3DUiService shape3DUiService;
    private Map<Integer, Shape3DConfig> changes = new HashMap<>();
    private Collection<Consumer<List<ObjectNameId>>> observers = new ArrayList<>();

    public void create(String colladaText) {
        caller.call(new RemoteCallback<Shape3D>() {
            @Override
            public void callback(Shape3D shape3D) {
                shape3DUiService.override(shape3D);
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).create(colladaText);
    }

    public void reload() {
        caller.call(new RemoteCallback<List<Shape3D>>() {
            @Override
            public void callback(List<Shape3D> shape3Ds) {
                changes.clear();
                shape3DUiService.setShapes3Ds(shape3Ds);
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).getShape3Ds();
    }

    public void updateCollada(Shape3D originalShape3D, String colladaText) {
        caller.call(new RemoteCallback<Shape3D>() {
            @Override
            public void callback(Shape3D shape3D) {
                shape3D.setDbId(originalShape3D.getDbId());
                shape3D.setModelMatrixAnimations(originalShape3D.getModelMatrixAnimations());
                Shape3DUtils.replaceTextureIds(originalShape3D, shape3D);
                shape3DUiService.override(shape3D);
                addChangesCollada(originalShape3D.getDbId(), colladaText);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).colladaConvert(colladaText);
    }

    public void updateTexture(Shape3D originalShape3D, String materialId, int imageId) {
        Shape3DUtils.replaceTextureId(originalShape3D, materialId, imageId);
        Shape3DConfig shape3DConfig = getChangedShape3DConfig(originalShape3D.getDbId());
        Map<String, Integer> textureMap = new HashMap<>();
        Shape3DUtils.getAllVertexContainers(originalShape3D).stream().filter(vertexContainer -> vertexContainer.getTextureId() != null).forEach(vertexContainer -> textureMap.put(vertexContainer.getMaterialId(), vertexContainer.getTextureId()));
        shape3DConfig.setTextures(textureMap);
        shape3DUiService.override(originalShape3D);
    }

    public void save(Shape3D shape3D) {
        Shape3DConfig shape3DConfig = changes.get(shape3D.getDbId());
        if (shape3DConfig == null) {
            return;
        }

        caller.call(response -> changes.remove(shape3D.getDbId()), (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.save failed: " + message, throwable);
            return false;
        }).save(shape3DConfig);
    }


    public void delete(Shape3D shape3D) {
        caller.call(response -> {
            shape3DUiService.remove(shape3D);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.delete failed: " + message, throwable);
            return false;
        }).delete(shape3D.getDbId());
    }

    private void addChangesCollada(int dbId, String colladaText) {
        Shape3DConfig shape3DConfig = getChangedShape3DConfig(dbId);
        shape3DConfig.setColladaString(colladaText);
    }

    private Shape3DConfig getChangedShape3DConfig(int dbId) {
        Shape3DConfig shape3DConfig = changes.get(dbId);
        if (shape3DConfig == null) {
            shape3DConfig = new Shape3DConfig();
            shape3DConfig.setDbId(dbId);
            changes.put(dbId, shape3DConfig);
        }
        return shape3DConfig;
    }

    public void monitor(Consumer<List<ObjectNameId>> observer) {
        observers.add(observer);
        observer.accept(setupObjectNameIds());
    }

    public void removeMonitor(Consumer<List<ObjectNameId>> observer) {
        observers.remove(observer);
    }

    private List<ObjectNameId> setupObjectNameIds() {
        return shape3DUiService.getShape3Ds().stream().map(Shape3D::createSlopeNameId).collect(Collectors.toList());
    }

    private void fire() {
        List<ObjectNameId> objectNameIds = setupObjectNameIds();
        for (Consumer<List<ObjectNameId>> observer : observers) {
            observer.accept(objectNameIds);
        }
    }
}
