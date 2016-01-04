package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.units.UnitService;
import com.btxtech.shared.VertexList;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Dependent
public class UnitRenderer extends AbstractRenderer {
    @Inject
    private UnitService unitService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private ShaderTextureCoordinateAttribute textureCoordinateAttribute;
    private WebGlUniformTexture texture;
    private int elementCount;
    // private Logger logger = Logger.getLogger(UnitRenderer.class.getName());

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.unitVertexShader(), Shaders.INSTANCE.unitFragmentShader());
        positions = createVertexShaderAttribute("aVertexPosition");
        norms = createVertexShaderAttribute("aVertexNormal");
        textureCoordinateAttribute = createShaderTextureCoordinateAttributee("aTextureCoord");
        texture = createWebGLTexture(unitService.getImageDescriptor(), "uSampler", WebGLRenderingContext.TEXTURE0, 0);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = unitService.getVertexList();
        if (vertexList == null) {
            return;
        }
        positions.fillBuffer(vertexList.getVertices());
        norms.fillBuffer(vertexList.getNormVertices());
        textureCoordinateAttribute.fillBuffer(vertexList.getTextureCoordinates());
        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv("uMMatrix", unitService.getModelMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uNMMatrix", unitService.getModelNormMatrix());
        uniformMatrix4fv("uNVMatrix", camera.createNormMatrix());
        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniform3f("uAmbientColor", lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        uniform3f("uLightingDirection", lighting.getLightDirection());
        uniform3f("uLightingColor", lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity());
        uniform1f("uSpecularHardness", unitService.getSpecularHardness());
        uniform1f("uSpecularIntensity", unitService.getSpecularIntensity());

        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
        texture.activate();

        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
