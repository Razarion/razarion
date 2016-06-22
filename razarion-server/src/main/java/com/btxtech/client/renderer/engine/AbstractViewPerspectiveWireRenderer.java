package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
public abstract class AbstractViewPerspectiveWireRenderer extends AbstractRenderer {
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute barycentric;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;

    protected abstract List<Vertex> getVertexList();

    protected abstract List<Vertex> getBarycentricList();

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }

        createProgram(Shaders.INSTANCE.viewPerspectiveWireVertexShader(), Shaders.INSTANCE.viewPerspectiveWireFragmentShader());

        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(A_BARYCENTRIC);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers() {
        List<Vertex> vertexList = getVertexList();
        vertices.fillBuffer(vertexList);
        barycentric.fillBuffer(getBarycentricList());
        setElementCount(vertexList.size());
    }


    @Override
    public void draw() {
        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());

        vertices.activate();
        barycentric.activate();

        drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
