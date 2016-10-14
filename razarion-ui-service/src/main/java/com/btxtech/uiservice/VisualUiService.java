package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.VisualConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.08.2016.
 */
@ApplicationScoped
public class VisualUiService {
    @Inject
    private Event<VisualConfig> visualConfigTrigger;
    private VisualConfig visualConfig;

    public void initialise(VisualConfig visualConfig) {
        this.visualConfig = visualConfig;
        visualConfigTrigger.fire(visualConfig);
    }

    public VisualConfig getVisualConfig() {
        return visualConfig;
    }

    public Vertex getShape3DLightDirection() {
        return Matrix4.createZRotation(visualConfig.getShape3DLightRotateZ()).multiply(Matrix4.createXRotation(visualConfig.getShape3DLightRotateX())).multiply(new Vertex(0, 0, -1), 1.0);
    }
}
