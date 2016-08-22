package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.shared.Shape3DProvider;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainer;
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
    private Collection<Consumer<List<Shape3D>>> observers = new ArrayList<>();

    public void create(String dataUrl) {
        caller.call(new RemoteCallback<Shape3D>() {
            @Override
            public void callback(Shape3D shape3D) {
                shape3DUiService.override(shape3D);
                fire(shape3DUiService.getShape3Ds());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).create(dataUrl);
    }

    public void reload() {
        caller.call(new RemoteCallback<List<Shape3D>>() {
            @Override
            public void callback(List<Shape3D> shape3Ds) {
                changes.clear();
                shape3DUiService.setShapes3Ds(shape3Ds);
                fire(shape3Ds);
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
        for (VertexContainer vertexContainer : Shape3DUtils.getAllVertexContainers(originalShape3D)) {
            if (vertexContainer.getTextureId() != null) {
                textureMap.put(vertexContainer.getMaterialId(), vertexContainer.getTextureId());
            }
        }
        shape3DConfig.setTextures(textureMap);
        shape3DUiService.override(originalShape3D);
    }

    public void save() {
        if (changes.isEmpty()) {
            return;
        }
        caller.call(response -> changes.clear(), (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.save failed: " + message, throwable);
            return false;
        }).save(new ArrayList<>(changes.values()));
    }


    public void delete(Shape3D shape3D) {
        caller.call(response -> {
            shape3DUiService.remove(shape3D);
            fire(shape3DUiService.getShape3Ds());
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

    public void monitor(Consumer<List<Shape3D>> observer) {
        observers.add(observer);
        observer.accept(shape3DUiService.getShape3Ds());
    }

    public void removeMonitor(Consumer<List<Shape3D>> observer) {
        observers.remove(observer);
    }

    private void fire(List<Shape3D> shape3Ds) {
        for (Consumer<List<Shape3D>> observer : observers) {
            observer.accept(shape3Ds);
        }
    }
}
