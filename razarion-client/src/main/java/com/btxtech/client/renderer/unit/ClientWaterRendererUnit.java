package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class ClientWaterRendererUnit extends AbstractWaterRendererUnit {
    // private Logger logger = Logger.getLogger(ClientWaterRendererUnit.class.getName());
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlFacade webGlFacade;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute norms;
    private Vec3Float32ArrayShaderAttribute tangents;
    private WebGlUniformTexture bumpMap;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillInternalBuffers(WaterUi waterUi) {
        positions.fillFloat32ArrayEmu(waterUi.getVertices());
        norms.fillFloat32ArrayEmu(waterUi.getNorms());
        tangents.fillFloat32ArrayEmu(waterUi.getTangents());
        bumpMap = webGlFacade.createWebGLBumpMapTexture(waterUi.getBmId(), "uBm", "uBmScale", waterUi.getBmScale(), "uBmOnePixel");
    }

    @Override
    public void draw(WaterUi waterUi) {
        webGlFacade.useProgram();

        webGlFacade.setLightUniforms(null, waterUi.getLightConfig());

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.getNormMatrix());
        webGlFacade.uniform1f("uTransparency", waterUi.getTransparency());
        webGlFacade.uniform1f("uBmDepth", waterUi.getBmDepth());
        webGlFacade.uniform1f("animation", waterUi.getWaterAnimation());
        webGlFacade.uniform1f("animation2", waterUi.getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.overrideScale(waterUi.getBmScale());
        bumpMap.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
