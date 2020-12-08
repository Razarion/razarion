package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.task.selection.ItemMarkerRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.selection.ItemMarkerGeometry;

import javax.enterprise.context.Dependent;

import static com.btxtech.client.renderer.webgl.WebGlFacade.U_COLOR;

/**
 * Created by Beat
 * 23.01.2017.
 */
@Dependent
public class ItemMarkerRenderTask extends AbstractWebGlRenderTask<ItemMarkerGeometry> implements ItemMarkerRenderTaskRunner.RenderTask {
    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(ItemMarkerGeometry itemMarkerGeometry) {
        return new WebGlFacadeConfig(Shaders.SHADERS.itemMarkerCustom())
                .enableTransformation(false)
                .blend(WebGlFacadeConfig.Blend.SOURCE_ALPHA)
                .depthTest(false)
                .writeDepthBuffer(false);
    }

    @Override
    protected void setup(ItemMarkerGeometry itemMarkerGeometry) {
        setupVec3VertexPositionArray(itemMarkerGeometry.getVertexes());
        setupVec1Array("aVisibility", itemMarkerGeometry.getVisibilities());

        setupModelMatrixUniform(U_COLOR, UniformLocation.Type.COLOR, ModelMatrices::getColor);
        setupModelMatrixUniform("uRadius", UniformLocation.Type.F, ModelMatrices::getRadius);
    }
}
