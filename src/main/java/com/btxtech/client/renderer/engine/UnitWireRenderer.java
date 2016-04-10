package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.units.UnitService;
import com.btxtech.shared.VertexList;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class UnitWireRenderer extends AbstractRenderer {
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentrics;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private int elementCount;
    // private Logger logger = Logger.getLogger(UnitWireRenderer.class.getName());
    @Inject
    private UnitService unitService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.modelViewPerspectiveWireVertexShader(), Shaders.INSTANCE.modelViewPerspectiveWireFragmentShader());
        positions = createVertexShaderAttribute("aVertexPosition");
        barycentrics = createVertexShaderAttribute("aBarycentric");
        textureCoordinate = createShaderTextureCoordinateAttributee("aTextureCoord");
    }

    @Override
    public void setupImages() {
        webGLTexture = createWebGLTexture(ImageDescriptor.CHESS_TEXTURE_08, "uSampler", WebGLRenderingContext.TEXTURE0, 0);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = unitService.getVertexList();
        if (vertexList == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexList.getVertices());
        barycentrics.fillBuffer(vertexList.getBarycentric());
        textureCoordinate.fillBuffer(vertexList.getTextureCoordinates());

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();

        uniformMatrix4fv("uMMatrix", unitService.getModelMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());

        positions.activate();
        barycentrics.activate();
        textureCoordinate.activate();
        webGLTexture.activate();

        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
