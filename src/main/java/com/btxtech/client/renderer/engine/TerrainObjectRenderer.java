package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.ViewTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class TerrainObjectRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String A_VERTEX_NORMAL = "aVertexNormal";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer normalBuffer;
    private int normalPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private WebGLTexture webGLTexture;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private Lighting lighting;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainObjectVertexShader(), Shaders.INSTANCE.terrainObjectFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        normalBuffer = gameCanvas.getCtx3d().createBuffer();
        normalPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_NORMAL);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
        webGLTexture = setupTexture(terrainObjectService.getImageDescriptor());
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainObjectService.getVertexList();
        if (vertexList == null) {
            elementCount = 0;
            return;
        }
        // vertices
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // barycentric
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, normalBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createNormPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // texture Coordinate
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();
        // Projection uniform
        WebGLUniformLocation perspectiveUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // View transformation uniform
        WebGLUniformLocation viewUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(viewUniform, false, WebGlUtil.createArrayBufferOfFloat32(viewTransformation.createMatrix().toWebGlArray()));
        // Model transformation uniform
        WebGLUniformLocation modelUniform = getUniformLocation(MODEL_UNIFORM_NAME);
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set the normals
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, normalBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);

        // Ambient color uniform
        WebGLUniformLocation pAmbientUniformColor = getUniformLocation(UNIFORM_AMBIENT_COLOR);
        gameCanvas.getCtx3d().uniform3f(pAmbientUniformColor, (float) lighting.getAmbientColor().getR(), (float) lighting.getAmbientColor().getG(), (float) lighting.getAmbientColor().getB());
        // Lighting direction uniform
        Vertex direction = lighting.getLightDirection();
        WebGLUniformLocation pLightingDirectionUniformColor = getUniformLocation(UNIFORM_LIGHTING_DIRECTION);
        gameCanvas.getCtx3d().uniform3f(pLightingDirectionUniformColor, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        // Lighting color uniform
        WebGLUniformLocation pLightingColorUniformColor = getUniformLocation(UNIFORM_DIRECTIONAL_COLOR);
        gameCanvas.getCtx3d().uniform3f(pLightingColorUniformColor, (float) lighting.getColor().getR(), (float) lighting.getColor().getG(), (float) lighting.getColor().getB());

        // Texture
        WebGLUniformLocation tUniform = getUniformLocation(SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().uniform1i(tUniform, 0);
        // Draw
        for (Matrix4 matrix4 : terrainObjectService.getPositions()) {
            // Model model transformation uniform
            gameCanvas.getCtx3d().uniformMatrix4fv(modelUniform, false, WebGlUtil.createArrayBufferOfFloat32(matrix4.toWebGlArray()));
            // Draw
            gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
        }
    }
}
