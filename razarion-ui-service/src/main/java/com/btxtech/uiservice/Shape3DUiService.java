package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.VisualConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 16.08.2016.
 */
public class Shape3DUiService {
    // private Logger logger = Logger.getLogger(Shape3DUiService.class.getName());
    private Map<Integer, Shape3D> shape3Ds = new HashMap<>();

    // Global methods  ----------------------------------------------------
    public void onVisualConfig(@Observes VisualConfig visualConfig) {
        setShapes3Ds(visualConfig.getShape3Ds());
    }

    public Shape3D getShape3D(int id) {
        return shape3Ds.get(id);
    }

    // Methods only used by the editor ----------------------------------------------------
    public List<Shape3D> getShape3Ds() {
        return new ArrayList<>(shape3Ds.values());
    }

    public void setShapes3Ds(List<Shape3D> shape3Ds) {
        this.shape3Ds.clear();
        if (shape3Ds != null) {
            for (Shape3D shape3D : shape3Ds) {
                this.shape3Ds.put(shape3D.getDbId(), shape3D);
            }
        }
    }

    public void override(Shape3D shape3D) {
        shape3Ds.put(shape3D.getDbId(), shape3D);
    }

    public void remove(Shape3D shape3D) {
        shape3Ds.remove(shape3D.getDbId());
    }
}
