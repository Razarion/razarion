package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
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
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private VertexShaderAttribute tangents;
    private WebGlUniformTexture bumpMap;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = createVertexShaderAttribute("aVertexPosition");
        norms = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
    }

    @Override
    public void setupImages() {
        bumpMap = createWebGLBumpMapTexture(terrainSurface.getWater().getBumpMap(), "uSamplerBumpMap", WebGLRenderingContext.TEXTURE0, 0); // TODO
    }

    @Override
    public void fillBuffers() {
        if (terrainSurface.getWater().getVertices().isEmpty()) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(terrainSurface.getWater().getVertices());
        norms.fillBuffer(terrainSurface.getWater().getNorms());
        tangents.fillBuffer(terrainSurface.getWater().getTangents());

        elementCount = terrainSurface.getWater().getVertices().size();
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);

        useProgram();

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());
        uniform3f("uLightingDirection", lighting.getLightDirection());
        uniform3f("uLightingColor", lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity());
        uniform3f("uAmbientColor", lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        uniform1i("uBumpMapSize", terrainSurface.getWater().getBumpMap().getQuadraticEdge());
        uniform1f("uTransparency", terrainSurface.getWater().getWaterTransparency());
        uniform1f("uBumpMapDepth", terrainSurface.getWater().getWaterBumpMapDepth());
        uniform1f("uSlopeSpecularHardness", terrainSurface.getWater().getWaterSpecularHardness());
        uniform1f("uSlopeSpecularIntensity", terrainSurface.getWater().getWaterSpecularIntensity());
        uniform1f("animation", terrainSurface.getWater().getWaterAnimation());
        uniform1f("animation2", terrainSurface.getWater().getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.activate();

        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());

        gameCanvas.getCtx3d().depthMask(true);
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
    }
}
