package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.slope.Mesh;
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
    private WebGlUniformTexture slopeWebGLTexture;
    private WebGlUniformTexture slopeBumpWebGLTexture;
    private WebGlUniformTexture groundWebGLTexture;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
        normals = createVertexShaderAttribute("aVertexNormal");
        tangents = createVertexShaderAttribute("aVertexTangent");
        slopeFactors = createFloatShaderAttribute("aSlopeFactor");
        slopeWebGLTexture = createWebGLTexture(terrainSurface.getPlateau().getMesh().getSlopeImageDescriptor(), "uSamplerSlopeTexture", WebGLRenderingContext.TEXTURE0, 0);
        slopeBumpWebGLTexture = createWebGLBumpMapTexture(terrainSurface.getPlateau().getMesh().getSlopeBumpImageDescriptor(), "uSamplerBumpMapSlopeTexture", WebGLRenderingContext.TEXTURE1, 1);
        groundWebGLTexture = createWebGLTexture(terrainSurface.getGroundImageDescriptor(), "uSamplerGroundCover", WebGLRenderingContext.TEXTURE2, 2);
    }

    @Override
    public void fillBuffers() {
        Mesh mesh = terrainSurface.getPlateau().getMesh();
        List<Vertex> vertexList = mesh.getVertices();
        vertices.fillBuffer(vertexList);
        normals.fillBuffer(mesh.getNorms());
        tangents.fillBuffer(mesh.getTangents());
        slopeFactors.fillFloatBuffer(mesh.getSlopeFactors());

        elementCount = vertexList.size();
    }


    @Override
    public void draw() {
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
        uniform1i("uSamplerSlopeTextureSize", terrainSurface.getPlateau().getMesh().getSlopeImageDescriptor().getQuadraticEdge());
        uniform1i("uSamplerBumpMapSlopeTextureSize", terrainSurface.getPlateau().getMesh().getSlopeBumpImageDescriptor().getQuadraticEdge());
        uniform1f("uBumpMapSlopeDepth", terrainSurface.getPlateauConfigEntity().getBumpMapDepth());
        uniform1f("slopeSpecularIntensity", terrainSurface.getPlateauConfigEntity().getSpecularIntensity());
        uniform1f("slopeSpecularHardness", terrainSurface.getPlateauConfigEntity().getSpecularHardness());
        uniform1i("uSamplerGroundCoverSize", terrainSurface.getGroundImageDescriptor().getQuadraticEdge());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();

        slopeWebGLTexture.activate();
        slopeBumpWebGLTexture.activate();
        groundWebGLTexture.activate();

        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
