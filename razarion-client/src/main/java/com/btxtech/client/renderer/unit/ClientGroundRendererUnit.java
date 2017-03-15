package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@ColorBufferRenderer
@Dependent
public class ClientGroundRendererUnit extends AbstractGroundRendererUnit {
    // private Logger logger = Logger.getLogger(ClientGroundRendererUnit.class.getName());
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private Camera camera;
    private Vec3Float32ArrayShaderAttribute vertices;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec3Float32ArrayShaderAttribute tangents;
    private Float32ArrayShaderAttribute splattings;
    private WebGlUniformTexture topTexture;
    private WebGlUniformTexture topBm;
    private WebGlUniformTexture splattingTexture;
    private WebGlUniformTexture bottomTexture;
    private WebGlUniformTexture bottomBm;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        splattings = webGlFacade.createFloat32ArrayShaderAttribute(WebGlFacade.A_GROUND_SPLATTING);
        webGlFacade.enableReceiveShadow();
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffersInternal(GroundUi groundUi) {
        topTexture = webGlFacade.createWebGLTexture(groundUi.getTopTextureId(), "uTopTexture", "uTopTextureScale", groundUi.getTopTextureScale());
        topBm = webGlFacade.createWebGLBumpMapTexture(groundUi.getTopBmId(), "uTopBm", "uTopBmScale", groundUi.getTopBmScale(), "uTopBmOnePixel");
        splattingTexture = webGlFacade.createWebGLTexture(groundUi.getSplattingId(), "uSplatting", "uSplattingScale", groundUi.getSplattingScale());
        bottomTexture = webGlFacade.createWebGLTexture(groundUi.getBottomTextureId(), "uBottomTexture", "uBottomTextureScale", groundUi.getBottomTextureScale());
        bottomBm = webGlFacade.createWebGLBumpMapTexture(groundUi.getBottomBmId(), "uBottomBm", "uBottomBmScale", groundUi.getBottomBmScale(), "uBottomBmOnePixel");

        vertices.fillFloat32ArrayEmu(groundUi.getVertices());
        normals.fillFloat32ArrayEmu(groundUi.getNorms());
        tangents.fillFloat32ArrayEmu(groundUi.getTangents());
        splattings.fillFloat32ArrayEmu(groundUi.getSplattings());
    }

    @Override
    public void draw(GroundUi groundUi) {
        webGlFacade.useProgram();
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.getNormMatrix());

        webGlFacade.setLightUniforms(null, groundUi.getGroundLightConfig());
        webGlFacade.uniform1f("uTopBmDepth", groundUi.getTopBmDepth());
        webGlFacade.uniform1f("uBottomBmDepth", groundUi.getBottomBmDepth());

        webGlFacade.activateReceiveShadow();

        vertices.activate();
        normals.activate();
        tangents.activate();
        splattings.activate();

        topTexture.overrideScale(groundUi.getTopTextureScale());
        topTexture.activate();
        topBm.overrideScale(groundUi.getTopBmScale());
        topBm.activate();
        splattingTexture.overrideScale(groundUi.getSplattingScale());
        splattingTexture.activate();
        bottomTexture.overrideScale(groundUi.getBottomTextureScale());
        bottomTexture.activate();
        bottomBm.overrideScale(groundUi.getBottomBmScale());
        bottomBm.activate();

        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
