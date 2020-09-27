package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.renderer.task.BaseItemPlacerRenderTaskRunner;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static com.btxtech.client.renderer.webgl.WebGlFacade.U_COLOR;
import static com.btxtech.uiservice.Colors.START_POINT_PLACER_IN_VALID;
import static com.btxtech.uiservice.Colors.START_POINT_PLACER_VALID;

/**
 * Created by Beat
 * 10.09.2016.
 */
@Dependent
public class BaseItemPlacerCircleRendererTask extends AbstractWebGlRenderTask<BaseItemPlacer> implements BaseItemPlacerRenderTaskRunner.Circle {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(BaseItemPlacer baseItemPlacer) {
        return new WebGlFacadeConfig(Shaders.INSTANCE.rgbaMvpVertexShader(), Shaders.INSTANCE.rgbaFragmentShader())
                .enableTransformation(false);
    }

    @Override
    protected void setup(BaseItemPlacer baseItemPlacer) {
        setupVec3PositionArray(jsInteropObjectFactory.newFloat32Array4Vertices(baseItemPlacer.getVertexes()));
        setupUniform(U_COLOR, UniformLocation.Type.COLOR, () -> baseItemPlacer.isPositionValid() ? START_POINT_PLACER_VALID : START_POINT_PLACER_IN_VALID);
    }

    @Override
    public void setModelMatricesSupplier(Function<Long, List<ModelMatrices>> modelMatricesSupplier) {
        super.setModelMatricesSupplier(modelMatricesSupplier);
    }
}
