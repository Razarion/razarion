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
    private WebGlUniformTexture slopeTexture;
    private WebGlUniformTexture uSlopeBm;
    private WebGlUniformTexture groundSplattingTexture;
    private WebGlUniformTexture groundTopTexture;
    private WebGlUniformTexture groundTopBm;
    private WebGlUniformTexture groundBottomTexture;
    private WebGlUniformTexture groundBottomBm;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
        normals = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
        slopeFactors = createFloatShaderAttribute("aSlopeFactor");
        groundSplatting = createFloatShaderAttribute("aGroundSplatting");
    }

    @Override
    public void setupImages() {
        Slope slope = terrainSurface.getSlope(getId());
        slopeTexture = createWebGLTexture(slope.getSlopeImageDescriptor(), "uSlopeTexture");
        uSlopeBm = createWebGLBumpMapTexture(slope.getSlopeBumpImageDescriptor(), "uSlopeBm");
        groundSplattingTexture = createWebGLTexture(terrainSurface.getSplatting(), "uGroundSplatting");
        groundTopTexture = createWebGLTexture(terrainSurface.getTopTexture(), "uGroundTopTexture");
        groundTopBm = createWebGLTexture(terrainSurface.getTopBm(), "uGroundTopBm");
        groundBottomTexture = createWebGLTexture(terrainSurface.getGroundTexture(), "uGroundBottomTexture");
        groundBottomBm = createWebGLTexture(terrainSurface.getGroundBm(), "uGroundBottomBm");
        enableShadow();
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

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());

        setLightUniforms("Slope", slope.getSlopeSkeleton().getLightConfig());
        setLightUniforms("Ground", terrainSurface.getGroundSkeleton().getLightConfig());

        uniform1b("slopeOriented", slope.getSlopeSkeleton().getSlopeOriented());

        // Slope
        uniform1i("uSlopeTextureSize", slope.getSlopeImageDescriptor().getQuadraticEdge());
        uniform1i("uSlopeBmSize", slope.getSlopeBumpImageDescriptor().getQuadraticEdge());
        uniform1f("uSlopeBmDepth", slope.getSlopeSkeleton().getBumpMapDepth());
        //Ground
        uniform1i("uGroundSplattingSize", terrainSurface.getSplatting().getQuadraticEdge());
        uniform1i("uGroundTopTextureSize", terrainSurface.getTopTexture().getQuadraticEdge());
        uniform1i("uGroundTopBmSize", terrainSurface.getTopBm().getQuadraticEdge());
        uniform1f("uGroundTopBmDepth", terrainSurface.getGroundSkeleton().getTopBmDepth());
        uniform1i("uGroundBottomTextureSize", terrainSurface.getGroundTexture().getQuadraticEdge());
        uniform1i("uGroundBottomBmSize", terrainSurface.getGroundBm().getQuadraticEdge());
        uniform1f("uGroundBottomBmDepth", terrainSurface.getGroundSkeleton().getBottomBmDepth());
        // Water
        uniform1b("uHasWater", slope.hasWater());
        uniform1f("uWaterLevel", slope.getWaterLevel());
        uniform1f("uWaterGround", slope.getWaterGround());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeTexture.activate();
        uSlopeBm.activate();
        groundSplattingTexture.activate();
        groundTopTexture.activate();
        groundBottomTexture.activate();
        groundBottomBm.activate();
        groundTopBm.activate();

        activateShadow();

        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
