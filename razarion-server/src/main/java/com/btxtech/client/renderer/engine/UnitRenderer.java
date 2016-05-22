package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.units.ItemService;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.dto.VertexContainer;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Dependent
public class UnitRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(UnitRenderer.class.getName());
    @Inject
    private ItemService itemService;
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

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.unitVertexShader(), Shaders.INSTANCE.unitFragmentShader());
        positions = createVertexShaderAttribute("aVertexPosition");
        norms = createVertexShaderAttribute("aVertexNormal");
        textureCoordinateAttribute = createShaderTextureCoordinateAttributee("aTextureCoord");
    }

    @Override
    public void setupImages() {
        texture = createWebGLTexture(itemService.getImageDescriptor(), "uSampler", WebGLRenderingContext.TEXTURE0, 0);
    }

    @Override
    public void fillBuffers() {
        VertexContainer vertexContainer = itemService.getItemTypeVertexContainer(getId());
        if (vertexContainer == null) {
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        norms.fillBuffer(vertexContainer.getNorms());
        textureCoordinateAttribute.fillBuffer(vertexContainer.getTextureCoordinates());
        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        Collection<ModelMatrices> modelMatrices = itemService.getModelMatrices(getId());
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }

        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uNVMatrix", camera.createNormMatrix());
        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniform3f("uAmbientColor", lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        uniform3f("uLightingDirection", lighting.getLightDirection());
        uniform3f("uLightingColor", lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity());
        uniform1f("uSpecularHardness", itemService.getSpecularHardness());
        uniform1f("uSpecularIntensity", itemService.getSpecularIntensity());

        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
        texture.activate();

        for (ModelMatrices model : modelMatrices) {
            uniformMatrix4fv("uMMatrix", model.getVertex());
            uniformMatrix4fv("uNMMatrix", model.getNorm());

            getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
        }
    }
}
