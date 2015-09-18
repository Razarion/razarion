package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.09.2015.
 */
@Dependent
public class MonitorRenderer extends AbstractRenderer {
    public static final int WIDTH = 256;
    public static final int HEIGHT = WIDTH;
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String COLOR_SAMPLER_UNIFORM_NAME = "uColorSampler";
    private static final String DEEP_SAMPLER_UNIFORM_NAME = "uDeepSampler";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer textureCoordinateBuffer;
    private int textureCoordinatePositionAttribute;
    private int elementCount;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.monitorVertexShader(), Shaders.INSTANCE.monitorFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        textureCoordinateBuffer = gameCanvas.getCtx3d().createBuffer();
        textureCoordinatePositionAttribute = getAndEnableAttributeLocation(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = new VertexList();
        double monitorWidth = 2.0 * WIDTH / (double) gameCanvas.getWidth();
        double monitorHeight = 2.0 * HEIGHT / (double) gameCanvas.getHeight();
        Triangle triangle = new Triangle(new Vertex(0, 0, 0), new TextureCoordinate(0, 0),
                new Vertex(monitorWidth, 0, 0), new TextureCoordinate(1, 0),
                new Vertex(0, monitorHeight, 0), new TextureCoordinate(0, 1));
        vertexList.add(triangle);
        triangle = new Triangle(new Vertex(monitorWidth, monitorHeight, 0), new TextureCoordinate(1, 1),
                new Vertex(0, monitorHeight, 0), new TextureCoordinate(0, 1),
                new Vertex(monitorWidth, 0, 0), new TextureCoordinate(1, 0));
        vertexList.add(triangle);

        // vertices
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // texture Coordinate
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createTextureDoubles()), WebGLRenderingContext.STATIC_DRAW);

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        useProgram();
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set vertices texture coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, textureCoordinateBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(textureCoordinatePositionAttribute, TextureCoordinate.getComponentCount(), WebGLRenderingContext.FLOAT, false, 0, 0);

        // Color Texture
        WebGLUniformLocation tColorUniform = getUniformLocation(COLOR_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE0);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getColorTexture());
        gameCanvas.getCtx3d().uniform1i(tColorUniform, 0);
        // Deep Texture
        WebGLUniformLocation tUniform = getUniformLocation(DEEP_SAMPLER_UNIFORM_NAME);
        gameCanvas.getCtx3d().activeTexture(WebGLRenderingContext.TEXTURE1);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
        gameCanvas.getCtx3d().uniform1i(tUniform, 1);

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
