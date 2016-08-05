package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.shaders.Shaders;
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
public class WaterUnitRenderer extends AbstractWebGlUnitRenderer {
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private VertexShaderAttribute tangents;
    private WebGlUniformTexture_OLD bumpMap;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        norms = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
    }

    @Override
    public void setupImages() {
        bumpMap = createWebGLBumpMapTexture(terrainUiService.getWaterBumpMap(), "uSamplerBumpMap");
    }

    @Override
    public void fillBuffers() {
        if (terrainUiService.getWater().getVertices().isEmpty()) {
            return;
        }
        positions.fillBuffer(terrainUiService.getWater().getVertices());
        norms.fillBuffer(terrainUiService.getWater().getNorms());
        tangents.fillBuffer(terrainUiService.getWater().getTangents());

        setElementCount(terrainUiService.getWater().getVertices().size());
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);

        useProgram();

        setLightUniforms(null, terrainUiService.getWater().getLightConfig());

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());
        uniform1i("uBumpMapSize", terrainUiService.getWaterBumpMap().getQuadraticEdge());
        uniform1f("uTransparency", terrainUiService.getWater().getWaterTransparency());
        uniform1f("uBumpMapDepth", terrainUiService.getWater().getWaterBumpMapDepth());
        uniform1f("animation", terrainUiService.getWater().getWaterAnimation());
        uniform1f("animation2", terrainUiService.getWater().getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.activate();

        drawArrays(WebGLRenderingContext.TRIANGLES);

        gameCanvas.getCtx3d().depthMask(true);
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
    }
}
