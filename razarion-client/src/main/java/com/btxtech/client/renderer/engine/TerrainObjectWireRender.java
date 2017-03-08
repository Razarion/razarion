package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
@Deprecated
public class TerrainObjectWireRender extends AbstractWebGlUnitRenderer {
    private Logger logger = Logger.getLogger(TerrainObjectWireRender.class.getName());
    //    @Inject
//    private TerrainObjectService terrainObjectService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture_OLD webGLTexture;
    private int terrainObjectId;

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }

        createProgram(Shaders.INSTANCE.modelViewPerspectiveWireVertexShader(), Shaders.INSTANCE.modelViewPerspectiveWireFragmentShader());

        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(A_BARYCENTRIC);
        textureCoordinate = createShaderTextureCoordinateAttributee(A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {
        // webGLTexture = createWebGLTexture(ImageDescriptor.CHESS_TEXTURE_08, SAMPLER_UNIFORM_NAME);
    }

    @Override
    public void fillBuffers(Object o) {

    }

    public void fillBuffers() {
        // TODO terrainObjectId = terrainObjectService.getTerrainObjectId4VertexContainer(getId());
//  TODO      VertexContainer vertexContainer = terrainObjectService.getVertexContainer(getId());
//  TODO      if (vertexContainer == null || vertexContainer.empty()) {
//  TODO          logger.warning("No vertices to render");
//  TODO          return;
//  TODO      }
//  TODO      positions.fillBuffer(vertexContainer.OLDgetVertices());
//  TODO      barycentric.fillBuffer(vertexContainer.generateBarycentric());
//  TODO      textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());
//
//  TODO      setElementCount(vertexContainer);
    }

    @Override
    protected void prepareDraw() {
        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.getMatrix());

        positions.activate();
        barycentric.activate();
        textureCoordinate.activate();

        webGLTexture.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {

    }

//  TODO   @Override
//  TODO   protected void modelDraw(ModelMatrices modelMatrices) {
//  TODO       uniformMatrix4fv(U_MODEL_MATRIX, modelMatrices.getModel());
//  TODO       drawArrays(WebGLRenderingContext.TRIANGLES);
//  TODO   }
}
