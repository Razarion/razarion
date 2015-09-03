package com.btxtech.client.renderer.engine;

import com.btxtech.client.GameCanvas;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent // Why is dependent needed???
public class LightTriangleRenderUnit extends AbstractTriangleRenderUnit {
    private static final String A_VERTEX_NORMAL = "aVertexNormal";
    private static final String UNIFORM_COLOR = "uColor";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";
    private Color color;
    private WebGLBuffer aVertexNormal;
    private int normalPositionAttribute;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Lighting lighting;

    public void init(Color color) {
        this.color = color;
        createProgram(Shaders.INSTANCE.LightVertexShader(), Shaders.INSTANCE.LightFragmentShader());
        aVertexNormal = gameCanvas.getCtx3d().createBuffer();
        normalPositionAttribute = getWebGlProgram().getAndEnableAttributeLocation(A_VERTEX_NORMAL);
    }

    @Override
    protected void fillCustomBuffers(VertexList vertexList) {
        // normal vector
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createNormPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        WebGlUtil.checkLastWebGlError("bufferData", gameCanvas.getCtx3d());
    }

    @Override
    protected void customDraw() {
        // Set the normals
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, aVertexNormal);
        WebGlUtil.checkLastWebGlError("bindBuffer", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().vertexAttribPointer(normalPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        WebGlUtil.checkLastWebGlError("vertexAttribPointer", gameCanvas.getCtx3d());
        // Color uniform
        WebGLUniformLocation pUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_COLOR);
        gameCanvas.getCtx3d().uniform4f(pUniformColor, (float) color.getR(), (float) color.getG(), (float) color.getB(), (float) color.getA());
        // Ambient color uniform
        WebGLUniformLocation pAmbientUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_AMBIENT_COLOR);
        gameCanvas.getCtx3d().uniform3f(pAmbientUniformColor, (float) lighting.getAmbientColor().getR(), (float) lighting.getAmbientColor().getG(), (float) lighting.getAmbientColor().getB());
        // Lighting direction uniform
        Vertex direction = lighting.getLightDirection();
        WebGLUniformLocation pLightingDirectionUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_LIGHTING_DIRECTION);
        gameCanvas.getCtx3d().uniform3f(pLightingDirectionUniformColor, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        // Lighting color uniform
        WebGLUniformLocation pLightingColorUniformColor = getWebGlProgram().getUniformLocation(UNIFORM_DIRECTIONAL_COLOR);
        gameCanvas.getCtx3d().uniform3f(pLightingColorUniformColor, (float) lighting.getColor().getR(), (float) lighting.getColor().getG(), (float) lighting.getColor().getB());
    }
}
