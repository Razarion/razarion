package com.btxtech.client.editor.widgets.marker;

import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.subtask.AbstractWebGlRenderTask;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * Created by Beat
 * 11.09.2015.
 */
@Dependent
public class TerrainMarkerRenderTask extends AbstractWebGlRenderTask<List<Vertex>> {
    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(List<Vertex> vertices) {
        return new WebGlFacadeConfig(Shaders.SHADERS.customRgba())
                .depthTest(false)
                .writeDepthBuffer(false);
    }

    @Override
    protected void setup(List<Vertex> vertices) {
        setupVec3VertexPositionArray(vertices);
        setupUniform(WebGlFacade.U_COLOR, UniformLocation.Type.COLOR, () -> new Color(0, 1, 0, 0.5));
    }
}
