package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.slope.Mesh;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2016.
 */
@Dependent
public class SlopeRenderer extends AbstractRenderer {
    // private static Logger logger = Logger.getLogger(SlopeRenderer.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private int elementCount;
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute slopeFactors;
    private FloatShaderAttribute groundSplatting;
    private WebGlUniformTexture slopeWebGLTexture;
    private WebGlUniformTexture slopeBumpWebGLTexture;
    private WebGlUniformTexture groundSplattingTexture;
    private WebGlUniformTexture groundTopWebGLTexture;
    private WebGlUniformTexture groundBottomWebGLTexture;
    private WebGlUniformTexture bumpMapGroundWebGlTexture;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
        normals = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
        slopeFactors = createFloatShaderAttribute("aSlopeFactor");
        groundSplatting = createFloatShaderAttribute("aGroundSplatting");
    }

    @Override
    public void setupImages() {
        Slope slope = terrainSurface.getSlope(getId());
        slopeWebGLTexture = createWebGLTexture(slope.getSlopeImageDescriptor(), "uSamplerSlopeTexture");
        slopeBumpWebGLTexture = createWebGLBumpMapTexture(slope.getSlopeBumpImageDescriptor(), "uSamplerBumpMapSlopeTexture");
        groundSplattingTexture = createWebGLTexture(terrainSurface.getSplatting(), "uGroundSplatting");
        groundTopWebGLTexture = createWebGLTexture(terrainSurface.getTopTexture(), "uGroundTopTexture");
        groundBottomWebGLTexture = createWebGLTexture(terrainSurface.getGroundTexture(), "uGroundBottomTexture");
        bumpMapGroundWebGlTexture = createWebGLTexture(terrainSurface.getGroundBm(), "uGroundBottomMap");
    }

    @Override
    public void fillBuffers() {
        Mesh mesh = terrainSurface.getSlope(getId()).getMesh();
        List<Vertex> vertexList = mesh.getVertices();
        vertices.fillBuffer(vertexList);
        normals.fillBuffer(mesh.getNorms());
        tangents.fillBuffer(mesh.getTangents());
        slopeFactors.fillFloatBuffer(mesh.getSlopeFactors());
        groundSplatting.fillFloatBuffer(mesh.getSplatting());

        elementCount = vertexList.size();
    }


    @Override
    public void draw() {
        Slope slope = terrainSurface.getSlope(getId());

        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        getCtx3d().depthMask(true);

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());

        setLightUniforms("Slope", slope.getSlopeSkeleton().getLightConfig());

        uniform1f("uLightSpecularIntensityGround", 0); // TODO
        uniform1f("uLightSpecularHardnessGround", 0); // TODO
        uniform1f("uGroundSplattingDistance", 1); // TODO

        uniform1f("uSlopeGroundBlur", slope.getSlopeSkeleton().getSlopeGroundBlur());
        uniform1i("uSamplerSlopeTextureSize", slope.getSlopeImageDescriptor().getQuadraticEdge());
        uniform1i("uSamplerBumpMapSlopeTextureSize", slope.getSlopeBumpImageDescriptor().getQuadraticEdge());
        uniform1f("uBumpMapSlopeDepth", slope.getSlopeSkeleton().getBumpMapDepth());
        uniform1i("uGroundSplattingSize", terrainSurface.getSplatting().getQuadraticEdge());
        uniform1i("uGroundTopTextureSize", terrainSurface.getTopTexture().getQuadraticEdge());
        uniform1i("uGroundBottomTextureSize", terrainSurface.getGroundTexture().getQuadraticEdge());
        uniform1i("uGroundBottomMapSize", terrainSurface.getGroundBm().getQuadraticEdge());
        uniform1f("uGroundBottomMapDepth", terrainSurface.getGroundSkeleton().getBottomBmDepth());
        uniform1b("uHasWater", slope.hasWater());
        uniform1f("uWaterLevel", slope.getWaterLevel());
        uniform1f("uWaterGround", slope.getWaterGround());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeWebGLTexture.activate();
        slopeBumpWebGLTexture.activate();
        groundSplattingTexture.activate();
        groundTopWebGLTexture.activate();
        groundBottomWebGLTexture.activate();
        bumpMapGroundWebGlTexture.activate();

        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
