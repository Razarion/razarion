package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.control.GameUiControlInitEvent;

import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 16.08.2016.
 */
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
        return shape3Ds.get(id);
    }

    public void overrideShape3DConfig(Shape3DConfig shape3DConfig) {
        if(shape3Ds.get(shape3DConfig.getId()) == null) {
            return;
        }
        shape3Ds.get(shape3DConfig.getId()).getElement3Ds().forEach(element3D -> element3D.getVertexContainers().forEach(vertexContainer -> {
            vertexContainer.setShape3DMaterialConfig(shape3DConfig.getShape3DMaterialConfigs()
                    .stream()
                    .filter(shape3DMaterialConfig -> shape3DMaterialConfig.getMaterialId().equals(vertexContainer.getShape3DMaterialConfig().getMaterialId()))
                    .findFirst()
                    .orElse(null));
        }));
    }
}
