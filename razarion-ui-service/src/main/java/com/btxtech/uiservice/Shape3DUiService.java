package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.utils.Shape3DUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.08.2016.
 */
public abstract class Shape3DUiService {
    private Logger logger = Logger.getLogger(Shape3DUiService.class.getName());
    private Map<Integer, Shape3D> cache = new HashMap<>();
    private VisualConfig visualConfig;

    public abstract void create(String dataUrl, final Consumer<List<Shape3D>> callback);

    public abstract void reload(final Consumer<List<Shape3D>> callback);

    protected abstract void convertColladaText(String colladaText, final Consumer<Shape3D> callback);

    public void initialize(List<Shape3D> shape3Ds, VisualConfig visualConfig) {
        this.visualConfig = visualConfig;
        fillShape3Ds(shape3Ds);
    }

    protected void fillShape3Ds(List<Shape3D> shape3Ds) {
        cache.clear();
        if (shape3Ds != null) {
            for (Shape3D shape3D : shape3Ds) {
                cache.put(shape3D.getDbId(), shape3D);
            }
        }
    }

    public void request(int shape3DId, Consumer<Shape3D> consumer, boolean keepInformed) {
        Shape3D shape3D = cache.get(shape3DId);
        if (shape3D != null) {
            consumer.accept(shape3D);
        } else {
            logger.warning("No Shape3D for Id: " + shape3DId);
        }
    }

    public double getShape3DGeneralScale() {
        return visualConfig.getShape3DGeneralScale();
    }

    public List<Shape3D> getAllShape3Ds() {
        return new ArrayList<>(cache.values());
    }

    public void overrideImage(String colladaText, Shape3D originalShape3D) {
        convertColladaText(colladaText, shape3D -> {
            shape3D.setDbId(originalShape3D.getDbId());
            shape3D.setModelMatrixAnimations(originalShape3D.getModelMatrixAnimations());
            Shape3DUtils.replaceTextureIds(originalShape3D, shape3D);
        });
    }
}
