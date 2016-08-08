package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractSlopeUnitRenderer;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.01.2016.
 */
@Dependent
public class ClientSlopeUnitRenderer extends AbstractSlopeUnitRenderer {
    // private static Logger logger = Logger.getLogger(ClientSlopeUnitRenderer.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
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
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        slopeFactors = webGlFacade.createFloatShaderAttribute("aSlopeFactor");
        groundSplatting = webGlFacade.createFloatShaderAttribute("aGroundSplatting");
    }

    @Override
    protected void fillBuffers(Slope slope) {
        slopeTexture = webGlFacade.createWebGLTexture(slope.getSlopeSkeletonConfig().getImageId(), "uSlopeTexture", "uSlopeTextureScale", slope.getSlopeSkeletonConfig().getImageScale());
        uSlopeBm = webGlFacade.createWebGLBumpMapTexture(slope.getSlopeSkeletonConfig().getBumpImageId(), "uSlopeBm", "uSlopeBmScale", slope.getSlopeSkeletonConfig().getBumpImageScale());
        groundSplattingTexture = webGlFacade.createWebGLTexture(terrainUiService.getSplatting(), "uGroundSplatting");
        groundTopTexture = webGlFacade.createWebGLTexture(terrainUiService.getTopTexture(), WebGlFacade.U_GROUND_TOP_TEXTURE);
        groundTopBm = webGlFacade.createWebGLTexture(terrainUiService.getTopBm(), WebGlFacade.U_GROUND_TOP_BM);
        groundBottomTexture = webGlFacade.createWebGLTexture(terrainUiService.getGroundTexture(), WebGlFacade.U_GROUND_BOTTOM_TEXTURE);
        groundBottomBm = webGlFacade.createWebGLTexture(terrainUiService.getGroundBm(), WebGlFacade.U_GROUND_BOTTOM_BM);

        webGlFacade.enableReceiveShadow();

        Mesh mesh = slope.getMesh();
        vertices.fillBuffer(mesh.getVertices());
        normals.fillBuffer(mesh.getNorms());
        tangents.fillBuffer(mesh.getTangents());
        slopeFactors.fillFloatBuffer(mesh.getSlopeFactors());
        groundSplatting.fillFloatBuffer(mesh.getSplatting());

        setElementCount(mesh);
    }


    @Override
    protected void draw(Slope slope) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.createNormMatrix());

        webGlFacade.setLightUniforms("Slope", slope.getSlopeSkeletonConfig().getLightConfig());
        webGlFacade.setLightUniforms("Ground", terrainUiService.getGroundSkeleton().getLightConfig());

        webGlFacade.uniform1b("slopeOriented", slope.getSlopeSkeletonConfig().getSlopeOriented());

        // Slope
        webGlFacade.uniform1f("uSlopeBmDepth", slope.getSlopeSkeletonConfig().getBumpMapDepth());
        //Ground
        webGlFacade.uniform1i("uGroundTopTextureSize", terrainUiService.getTopTexture().getQuadraticEdge());
        webGlFacade.uniform1i("uGroundTopBmSize", terrainUiService.getTopBm().getQuadraticEdge());
        webGlFacade.uniform1f("uGroundTopBmDepth", terrainUiService.getGroundSkeleton().getTopBmDepth());
        webGlFacade.uniform1i("uGroundBottomTextureSize", terrainUiService.getGroundTexture().getQuadraticEdge());
        webGlFacade.uniform1i("uGroundBottomBmSize", terrainUiService.getGroundBm().getQuadraticEdge());
        webGlFacade.uniform1f("uGroundBottomBmDepth", terrainUiService.getGroundSkeleton().getBottomBmDepth());
        webGlFacade.uniform1i("uGroundSplattingSize", terrainUiService.getSplatting().getQuadraticEdge());
        // Water
        webGlFacade.uniform1b("uHasWater", slope.hasWater());
        webGlFacade.uniform1f("uWaterLevel", slope.getWaterLevel());
        webGlFacade.uniform1f("uWaterGround", slope.getWaterGround());

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

        webGlFacade.activateReceiveShadow();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void setupImages() {

    }
}
