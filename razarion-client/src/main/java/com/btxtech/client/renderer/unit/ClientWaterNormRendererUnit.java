package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderUtil;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@NormRenderer
@Dependent
public class ClientWaterNormRendererUnit extends AbstractWaterRendererUnit {
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlFacade webGlFacade;
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
    protected void fillInternalBuffers(Water water, VisualConfig visualConfig) {
        vertices.fillDoubleBuffer(RenderUtil.setupNormDoubles(water.getVertices(), water.getNorms()));
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, Matrix4.createIdentity());

        vertices.activate();
        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }
}
