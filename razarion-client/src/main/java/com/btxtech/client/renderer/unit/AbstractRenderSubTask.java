package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.engine.TextureIdHandler;
import com.btxtech.client.renderer.engine.WebGlGroundMaterial;
import com.btxtech.client.renderer.engine.shaderattribute.AbstractShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderSubTask;
import com.btxtech.uiservice.renderer.ViewService;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.base.Js;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractRenderSubTask<T> implements RenderSubTask<T> {
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private ViewService viewService;
    @Inject
    private VisualUiService visualUiService;
    private WebGlFacadeConfig webGlFacadeConfig;

    private Collection<AbstractShaderAttribute> arrays = new ArrayList<>();
    private int elementCount;
    private LightUniforms lightUniforms;
    private Collection<WebGlGroundMaterial> webGlGroundMaterials = new ArrayList<>();
    // Transformation
    private WebGLUniformLocation viewMatrixUniformLocation;
    private WebGLUniformLocation viewNormMatrixUniformLocation;
    private WebGLUniformLocation perspectiveMatrixUniformLocation;
    private WebGLUniformLocation receiveShadowMatrixUniformLocation;
    // Shadow lookup
    private TextureIdHandler.WebGlTextureId shadowWebGlTextureId;
    private WebGLUniformLocation uShadowAlpha;
    private WebGLUniformLocation uShadowTexture;

    protected abstract WebGlFacadeConfig getWebGlFacadeConfig(T t);

    protected abstract void setup(T t);

    public void init(T t) {
        webGlFacadeConfig = getWebGlFacadeConfig(t);
        if (webGlFacadeConfig.isOESStandardDerivatives()) {
            webGlFacade.enableOESStandartDerivatives();
        }
        webGlFacade.init(webGlFacadeConfig);
        setupTransformation();
        setupReceiveShadow();
        if (webGlFacadeConfig.isLight()) {
            lightUniforms = new LightUniforms(webGlFacade);
        }
        setup(t);
    }

    protected void setupTransformation() {
        if (webGlFacadeConfig.isTransformation()) {
            if (webGlFacadeConfig.isNormTransformation()) {
                viewMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
                viewNormMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_VIEW_NORM_MATRIX);
                perspectiveMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
            } else {
                viewMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
                perspectiveMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
            }
        }
    }

    protected void setupReceiveShadow() {
        if (webGlFacadeConfig.isReceiveShadow()) {
            receiveShadowMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_SHADOW_MATRIX);
            shadowWebGlTextureId = webGlFacade.createWebGlTextureId();
            uShadowAlpha = webGlFacade.getUniformLocation("uShadowAlpha");
            uShadowTexture = webGlFacade.getUniformLocation("uDepthTexture");
        }
    }

    protected void setupVec3Array(String name, Float32ArrayEmu float32ArrayEmu) {
        Vec3Float32ArrayShaderAttribute array = webGlFacade.createVec3Float32ArrayShaderAttribute(name);
        array.fillFloat32Array(Js.uncheckedCast(float32ArrayEmu));
        arrays.add(array);
    }

    protected void setupVec3PositionArray(Float32ArrayEmu float32ArrayEmu) {
        setupVec3Array(WebGlFacade.A_VERTEX_POSITION, float32ArrayEmu);
        elementCount = (int) (((Float32Array) (Js.uncheckedCast(float32ArrayEmu))).length / Vertex.getComponentsPerVertex());
    }

    protected void setupGroundMaterial(GroundConfig groundConfig) {
        webGlGroundMaterials.add(new WebGlGroundMaterial(webGlFacade, groundConfig));
    }

    @Override
    public void draw(List<ModelMatrices> modelMatrices, double interpolationFactor) {
        if (canBeSkipped()) {
            return;
        }
        webGlFacade.useProgram();
        transformationUniformValues();

        if (lightUniforms != null) {
            lightUniforms.setLightUniforms(webGlFacade);
        }

        activateReceiveShadow();

        arrays.forEach(AbstractShaderAttribute::activate);

        webGlGroundMaterials.forEach(WebGlGroundMaterial::activate);

//        if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
//            terrainMarkerTexture.activate();
//            webGlFacade.uniform4f(terrainMarker2DPoints, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundary());
//            webGlFacade.uniform1f(terrainMarkerAnimation, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getAnimation());
//        } else {
//            webGlFacade.uniform4f(terrainMarker2DPoints, 0, 0, 0, 0);
//        }
        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES, elementCount, getHelperString());
    }

    protected void transformationUniformValues() {
        switch (renderService.getPass()) {
            case MAIN:
                if (viewMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(viewMatrixUniformLocation, viewService.getViewMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", webGlFacade.getCtx3d());
                }
                if (viewNormMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(viewNormMatrixUniformLocation, viewService.getViewNormMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_NORM_MATRIX", webGlFacade.getCtx3d());
                }
                if (perspectiveMatrixUniformLocation != null) {
                    // Perspective
                    webGlFacade.uniformMatrix4fv(perspectiveMatrixUniformLocation, viewService.getPerspectiveMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", webGlFacade.getCtx3d());
                }
                if (receiveShadowMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(receiveShadowMatrixUniformLocation, viewService.getShadowLookupMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv uShadowMatrix", webGlFacade.getCtx3d());
                }
                break;
            case SHADOW:
                if (viewMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(viewMatrixUniformLocation, viewService.getViewShadowMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", webGlFacade.getCtx3d());
                }
                if (perspectiveMatrixUniformLocation != null) {
                    // Perspective
                    webGlFacade.uniformMatrix4fv(perspectiveMatrixUniformLocation, viewService.getPerspectiveShadowMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", webGlFacade.getCtx3d());
                }
                break;
            default:
                throw new IllegalStateException("Dont know how to setup transformation uniforms for render pass: " + renderService.getPass());
        }
    }

    protected void activateReceiveShadow() {
        if (shadowWebGlTextureId == null || renderService.getPass() == RenderService.Pass.SHADOW) {
            return;
        }
        webGlFacade.uniform1f(uShadowAlpha, (float) visualUiService.getPlanetVisualConfig().getShadowAlpha());
        webGlFacade.uniform1i(uShadowTexture, shadowWebGlTextureId.getUniformValue());
        webGlFacade.getCtx3d().activeTexture(shadowWebGlTextureId.getWebGlTextureId());
        webGlFacade.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
    }


    protected boolean canBeSkipped() {
        return renderService.getPass() == RenderService.Pass.SHADOW && !webGlFacadeConfig.isCastShadow();
    }

    public void dispose() {
        arrays.forEach(AbstractShaderAttribute::deleteBuffer);
    }

    protected String getHelperString() {
        return getClass().getName();
    }

}
