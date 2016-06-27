package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainSurface;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WaterRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private VertexShaderAttribute tangents;
    private WebGlUniformTexture bumpMap;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        norms = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
    }

    @Override
    public void setupImages() {
        bumpMap = createWebGLBumpMapTexture(terrainSurface.getWater().getBumpMap(), "uSamplerBumpMap");
    }

    @Override
    public void fillBuffers() {
        if (terrainSurface.getWater().getVertices().isEmpty()) {
            return;
        }
        positions.fillBuffer(terrainSurface.getWater().getVertices());
        norms.fillBuffer(terrainSurface.getWater().getNorms());
        tangents.fillBuffer(terrainSurface.getWater().getTangents());

        setElementCount(terrainSurface.getWater().getVertices().size());
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);

        useProgram();

        setLightUniforms(null, terrainSurface.getWater().getLightConfig());

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());
        uniform1i("uBumpMapSize", terrainSurface.getWater().getBumpMap().getQuadraticEdge());
        uniform1f("uTransparency", terrainSurface.getWater().getWaterTransparency());
        uniform1f("uBumpMapDepth", terrainSurface.getWater().getWaterBumpMapDepth());
        uniform1f("animation", terrainSurface.getWater().getWaterAnimation());
        uniform1f("animation2", terrainSurface.getWater().getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.activate();

        drawArrays(WebGLRenderingContext.TRIANGLES);

        gameCanvas.getCtx3d().depthMask(true);
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
    }
}
