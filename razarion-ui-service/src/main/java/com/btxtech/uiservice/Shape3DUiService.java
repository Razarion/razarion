package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.VisualConfig;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
public class Shape3DUiService {
    private Logger logger = Logger.getLogger(Shape3DUiService.class.getName());
    private Map<Integer, Shape3D> cache = new HashMap<>();
    private VisualConfig visualConfig;
    private MapCollection<Integer, Consumer<Shape3D>> shape3DObserver = new MapCollection<>();

    // Global methods  ----------------------------------------------------
    public void initialize(List<Shape3D> shape3Ds, VisualConfig visualConfig) {
        this.visualConfig = visualConfig;
        setShapes3Ds(shape3Ds);
    }

    @Deprecated
    // TODO too comples in render engine. Editor should handle this
    public void request(int shape3DId, Consumer<Shape3D> observer) {
        Shape3D shape3D = cache.get(shape3DId);
        shape3DObserver.put(shape3DId, observer);
        if (shape3D != null) {
            observer.accept(shape3D);
        } else {
            logger.warning("No Shape3D for Id: " + shape3DId);
        }
    }

    public Shape3D getShape3D(int id) {
        return cache.get(id);
    }

    public double getShape3DGeneralScale() {
        return visualConfig.getShape3DGeneralScale();
    }

    // Methods only used by the editor ----------------------------------------------------
    public List<Shape3D> getShape3Ds() {
        return new ArrayList<>(cache.values());
    }

    public void setShapes3Ds(List<Shape3D> shape3Ds) {
        cache.clear();
        if (shape3Ds != null) {
            for (Shape3D shape3D : shape3Ds) {
                cache.put(shape3D.getDbId(), shape3D);
            }
        }
    }

    public void removeShape3DObserver(Integer shape3DId, Consumer<Shape3D> observer) {
        shape3DObserver.remove(shape3DId, observer);
    }

    public void override(Shape3D shape3D) {
        cache.put(shape3D.getDbId(), shape3D);
        for (Consumer<Shape3D> consumer : shape3DObserver.getSave(shape3D.getDbId())) {
            consumer.accept(shape3D);
        }
    }

    public void remove(Shape3D shape3D) {
        cache.remove(shape3D.getDbId());
    }
}
