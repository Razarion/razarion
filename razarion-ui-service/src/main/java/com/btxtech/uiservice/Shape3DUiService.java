package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.control.GameUiControlInitEvent;

import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Deprecated // Use ThreeJsModel
public abstract class Shape3DUiService {
    // private Logger logger = Logger.getLogger(Shape3DUiService.class.getName());
    private Map<Integer, Shape3D> shape3Ds = new HashMap<>();

    public abstract double getMaxZ(VertexContainer vertexContainer);

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        this.shape3Ds.clear();
        if (gameUiControlInitEvent.getColdGameUiContext().getShape3Ds() != null) {
            for (Shape3D shape3D : gameUiControlInitEvent.getColdGameUiContext().getShape3Ds()) {
                this.shape3Ds.put(shape3D.getId(), shape3D);
            }
        }
    }

    public Shape3D getShape3D(int id) {
        Shape3D shape3D = shape3Ds.get(id);
        if (shape3D == null) {
            throw new IllegalArgumentException("No Shape3D for id: " + id);
        }
        return shape3D;
    }

    public void editorOverrideShape3D(Shape3D shape3D) {
        shape3Ds.put(shape3D.getId(), shape3D);
    }
}
