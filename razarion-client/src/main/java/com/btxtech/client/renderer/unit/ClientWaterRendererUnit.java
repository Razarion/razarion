package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

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
    private WebGlFacade webGlFacade;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute norms;
    private Vec3Float32ArrayShaderAttribute tangents;
    private WebGlUniformTexture bumpMap;
    private LightUniforms lightUniforms;
    private WebGLUniformLocation uTransparency;
    private WebGLUniformLocation uBmDepth;
    private WebGLUniformLocation animation;
    private WebGLUniformLocation animation2;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader()).enableTransformation(true));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        lightUniforms = new LightUniforms(null, webGlFacade);
        uTransparency = webGlFacade.getUniformLocation("uTransparency");
        uBmDepth = webGlFacade.getUniformLocation("uBmDepth");
        animation = webGlFacade.getUniformLocation("animation");
        animation2 = webGlFacade.getUniformLocation("animation2");
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

        lightUniforms.setLightUniforms(waterUi.getLightConfig(), webGlFacade);

        webGlFacade.uniform1f(uTransparency, waterUi.getTransparency());
        webGlFacade.uniform1f(uBmDepth, waterUi.getBmDepth());
        webGlFacade.uniform1f(animation, waterUi.getWaterAnimation());
        webGlFacade.uniform1f(animation2, waterUi.getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.overrideScale(waterUi.getBmScale());
        bumpMap.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
