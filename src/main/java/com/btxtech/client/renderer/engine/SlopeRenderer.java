package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.slope.Mesh;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.01.2016.
 */
@Dependent
public class SlopeRenderer extends AbstractRenderer {
    private static Logger logger = Logger.getLogger(SlopeRenderer.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Lighting lighting;
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
        slopeWebGLTexture = createWebGLTexture(slope.getSlopeImageDescriptor(), "uSamplerSlopeTexture", WebGLRenderingContext.TEXTURE0, 0);
        logger.severe("SlopeRenderer1: " + getId() + " " + slope.getSlopeImageDescriptor().getUrl());
        slopeBumpWebGLTexture = createWebGLBumpMapTexture(slope.getSlopeBumpImageDescriptor(), "uSamplerBumpMapSlopeTexture", WebGLRenderingContext.TEXTURE1, 1);
        logger.severe("SlopeRenderer1: " + getId() + " " + slope.getSlopeBumpImageDescriptor().getUrl());
        groundSplattingTexture = createWebGLTexture(terrainSurface.getBlenderImageDescriptor(), "uGroundSplatting", WebGLRenderingContext.TEXTURE2, 2);
        groundTopWebGLTexture = createWebGLTexture(terrainSurface.getCoverImageDescriptor(), "uGroundTopTexture", WebGLRenderingContext.TEXTURE3, 3);
        groundBottomWebGLTexture = createWebGLTexture(terrainSurface.getGroundImageDescriptor(), "uGroundBottomTexture", WebGLRenderingContext.TEXTURE4, 4);
        bumpMapGroundWebGlTexture = createWebGLTexture(terrainSurface.getGroundBmImageDescriptor(), "uGroundBottomMap", WebGLRenderingContext.TEXTURE5, 5);
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
        SlopeConfigEntity slopeConfigEntity = slope.getSlopeConfigEntity();

        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        getCtx3d().depthMask(true);

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uNMatrix", camera.createNormMatrix());
        uniform3f("uAmbientColor", lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        Vertex direction = lighting.getLightDirection();
        uniform3f("uLightingDirection", direction.getX(), direction.getY(), direction.getZ());
        uniform1f("diffuseWeightFactor", lighting.getDiffuseIntensity());
        uniform1f("uSlopeFactorDistance", slopeConfigEntity.getSlopeFactorDistance());
        uniform1i("uSamplerSlopeTextureSize", slope.getSlopeImageDescriptor().getQuadraticEdge());
        uniform1i("uSamplerBumpMapSlopeTextureSize", slope.getSlopeBumpImageDescriptor().getQuadraticEdge());
        uniform1f("uBumpMapSlopeDepth", slopeConfigEntity.getBumpMapDepth());
        uniform1f("slopeSpecularIntensity", slopeConfigEntity.getSpecularIntensity());
        uniform1f("slopeSpecularHardness", slopeConfigEntity.getSpecularHardness());
        uniform1i("uGroundSplattingSize", terrainSurface.getBlenderImageDescriptor().getQuadraticEdge());
        uniform1i("uGroundTopTextureSize", terrainSurface.getCoverImageDescriptor().getQuadraticEdge());
        uniform1i("uGroundBottomTextureSize", terrainSurface.getGroundImageDescriptor().getQuadraticEdge());
        uniform1i("uGroundBottomMapSize", terrainSurface.getGroundBmImageDescriptor().getQuadraticEdge());
        uniform1f("uGroundBottomMapDepth", terrainSurface.getGroundBumpMap());
        uniform1f("uGroundSplattingDistance", terrainSurface.getSplattingBlur());

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
