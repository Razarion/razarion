package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.datatypes.Vertex;
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
public class SlopeUnitRenderer extends AbstractWebGlUnitRenderer {
    // private static Logger logger = Logger.getLogger(SlopeUnitRenderer.class.getName());
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
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
        Slope slope = terrainUiService.getSlope(getId());
        slopeTexture = createWebGLTexture(slope.getSlopeSkeletonConfig().getImageId(), "uSlopeTexture");
        uSlopeBm = createWebGLBumpMapTexture(slope.getSlopeSkeletonConfig().getBumpImageId(), "uSlopeBm");
        groundSplattingTexture = createWebGLTexture(terrainUiService.getSplatting(), "uGroundSplatting");
        groundTopTexture = createWebGLTexture(terrainUiService.getTopTexture(), "uGroundTopTexture");
        groundTopBm = createWebGLTexture(terrainUiService.getTopBm(), "uGroundTopBm");
        groundBottomTexture = createWebGLTexture(terrainUiService.getGroundTexture(), "uGroundBottomTexture");
        groundBottomBm = createWebGLTexture(terrainUiService.getGroundBm(), "uGroundBottomBm");
        enableShadow();
    }

    @Override
    public void fillBuffers() {
        Mesh mesh = terrainUiService.getSlope(getId()).getMesh();
        List<Vertex> vertexList = mesh.getVertices();
        vertices.fillBuffer(vertexList);
        normals.fillBuffer(mesh.getNorms());
        tangents.fillBuffer(mesh.getTangents());
        slopeFactors.fillFloatBuffer(mesh.getSlopeFactors());
        groundSplatting.fillFloatBuffer(mesh.getSplatting());

        setElementCount(mesh);
    }


    @Override
    public void draw() {
        Slope slope = terrainUiService.getSlope(getId());

        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        getCtx3d().depthMask(true);

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());

        setLightUniforms("Slope", slope.getSlopeSkeletonConfig().getLightConfig());
        setLightUniforms("Ground", terrainUiService.getGroundSkeleton().getLightConfig());

        uniform1b("slopeOriented", slope.getSlopeSkeletonConfig().getSlopeOriented());

        // Slope
        // TODO SIZE uniform1i("uSlopeTextureSize", slope.getSlopeImageDescriptor().getQuadraticEdge());
        // TODO SIZE uniform1i("uSlopeBmSize", slope.getSlopeBumpImageDescriptor().getQuadraticEdge());
        uniform1f("uSlopeBmDepth", slope.getSlopeSkeletonConfig().getBumpMapDepth());
        //Ground
        uniform1i("uGroundSplattingSize", terrainUiService.getSplatting().getQuadraticEdge());
        uniform1i("uGroundTopTextureSize", terrainUiService.getTopTexture().getQuadraticEdge());
        uniform1i("uGroundTopBmSize", terrainUiService.getTopBm().getQuadraticEdge());
        uniform1f("uGroundTopBmDepth", terrainUiService.getGroundSkeleton().getTopBmDepth());
        uniform1i("uGroundBottomTextureSize", terrainUiService.getGroundTexture().getQuadraticEdge());
        uniform1i("uGroundBottomBmSize", terrainUiService.getGroundBm().getQuadraticEdge());
        uniform1f("uGroundBottomBmDepth", terrainUiService.getGroundSkeleton().getBottomBmDepth());
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

        drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
