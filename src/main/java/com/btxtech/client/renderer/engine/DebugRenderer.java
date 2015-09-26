package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.renderer.model.ViewTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.Cuboid;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Sphere;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 17.09.2015.
 */
@Dependent
public class DebugRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String A_COLOR = "aColor";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    // private Logger logger = Logger.getLogger(DebugRenderer.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    @Normal
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private Shadowing shadowing;
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer colorBuffer;
    private int colorAttribute;
    private int elementCount;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.debugVertexShader(), Shaders.INSTANCE.debugFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        colorBuffer = gameCanvas.getCtx3d().createBuffer();
        colorAttribute = getAndEnableAttributeLocation(A_COLOR);
    }

    @Override
    public void fillBuffers() {
        Cuboid cuboid = new Cuboid(1.0, 1.0, 3.0);
        VertexList vertexList = cuboid.provideVertexList();

        // vertices
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, colorBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createColorDoubles()), WebGLRenderingContext.STATIC_DRAW);

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        useProgram();

        // Projection uniform
        WebGLUniformLocation perspectiveUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // View transformation uniform
        WebGLUniformLocation viewUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(viewUniform, false, WebGlUtil.createArrayBufferOfFloat32(viewTransformation.createMatrix().toWebGlArray()));
        // Model transformation uniform
        WebGLUniformLocation modelUniform = getUniformLocation(MODEL_UNIFORM_NAME);
        Matrix4 modelMatrix4 = Matrix4.createTranslation(shadowing.getX(), shadowing.getY(), shadowing.getZ());
        modelMatrix4 = modelMatrix4.multiply(Matrix4.createZRotation(shadowing.getRotateZ()).multiply(Matrix4.createXRotation(shadowing.getRotateX())));
        gameCanvas.getCtx3d().uniformMatrix4fv(modelUniform, false, WebGlUtil.createArrayBufferOfFloat32(modelMatrix4.toWebGlArray()));
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, colorBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(colorAttribute, Color.getComponentsPerColorAlpha(), WebGLRenderingContext.FLOAT, false, 0, 0);

        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
