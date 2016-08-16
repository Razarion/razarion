package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.VisualConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.08.2016.
 */
@ApplicationScoped
public class Shape3DUiService {
    private Logger logger = Logger.getLogger(Shape3DUiService.class.getName());
    private Map<Integer, Shape3D> cache = new HashMap<>();
    private VisualConfig visualConfig;

    public void initialize(List<Shape3D> shape3Ds, VisualConfig visualConfig) {
        this.visualConfig = visualConfig;
        cache.clear();
        if (shape3Ds != null) {
            for (Shape3D shape3D : shape3Ds) {
                cache.put(shape3D.getDbId(), shape3D);
            }
        }
    }

    public void get(int shape3DId, Consumer<Shape3D> consumer) {
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
}
