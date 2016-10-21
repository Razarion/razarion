package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderUtil;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@NormRenderer
@Dependent
public class ClientGroundNormRendererUnit extends AbstractGroundRendererUnit {
    // private Logger logger = Logger.getLogger(ClientGroundNormRendererUnit.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private Camera camera;
    private VertexShaderAttribute vertices;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.debugVectorVertexShader(), Shaders.INSTANCE.debugVectorFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffers(VertexList vertexList, GroundSkeletonConfig groundSkeletonConfig) {
        vertexList.verify();

        vertices.fillDoubleBuffer(RenderUtil.setupNormDoubles(vertexList.getVertices(), vertexList.getNormVertices()));
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, Matrix4.createIdentity());

        vertices.activate();
        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }
}
