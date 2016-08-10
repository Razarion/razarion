package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.renderer.AbstractWaterRendererUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class ClientWaterRendererUnit extends AbstractWaterRendererUnit {
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private VertexShaderAttribute tangents;
    private WebGlUniformTexture bumpMap;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffers(Water water) {
        positions.fillBuffer(water.getVertices());
        norms.fillBuffer(water.getNorms());
        tangents.fillBuffer(water.getTangents());
        bumpMap = webGlFacade.createWebGLBumpMapTexture(terrainUiService.getWaterBumpMap(), "uSamplerBumpMap");
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);

        webGlFacade.useProgram();

        webGlFacade.setLightUniforms(null, terrainUiService.getWater().getLightConfig());

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.createNormMatrix());
        webGlFacade.uniform1i("uBumpMapSize", terrainUiService.getWaterBumpMap().getQuadraticEdge());
        webGlFacade.uniform1f("uTransparency", terrainUiService.getWater().getWaterTransparency());
        webGlFacade.uniform1f("uBumpMapDepth", terrainUiService.getWater().getWaterBumpMapDepth());
        webGlFacade.uniform1f("animation", terrainUiService.getWater().getWaterAnimation());
        webGlFacade.uniform1f("animation2", terrainUiService.getWater().getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);

        gameCanvas.getCtx3d().depthMask(true);
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
    }
}
