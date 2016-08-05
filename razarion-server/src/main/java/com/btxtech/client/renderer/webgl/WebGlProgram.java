package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.GameCanvas;
import elemental.html.WebGLProgram;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLShader;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.04.2015.
 */
@Dependent
public class WebGlProgram {
    private WebGLProgram program;
    private WebGLShader vs;
    private WebGLShader fs;
    @Inject
    private GameCanvas gameCanvas;

    public void createProgram(String vertexShaderCode, String fragmentShaderCode) {
        vs = createShader(WebGLRenderingContext.VERTEX_SHADER, vertexShaderCode);
        fs = createShader(WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderCode);
        program = createAndUseProgram(vs, fs);
    }

    public int getAndEnableAttributeLocation(String attributeName) {
        int attributeLocation = gameCanvas.getCtx3d().getAttribLocation(program, attributeName);
        if (attributeLocation == -1) {
            throw new IllegalArgumentException("No attribute location for '" + attributeName + "' in OpenGl program.");
        }
        gameCanvas.getCtx3d().enableVertexAttribArray(attributeLocation);
        WebGlUtil.checkLastWebGlError("enableVertexAttribArray",gameCanvas.getCtx3d());
        return attributeLocation;
    }

    public WebGLUniformLocation getUniformLocation(String uniformName) {
        WebGLUniformLocation uniform = gameCanvas.getCtx3d().getUniformLocation(program, uniformName);
        if (uniform == null) {
            throw new IllegalArgumentException("No uniform location for '" + uniformName + "' in OpenGl program.");
        }
        return uniform;
    }

    public void useProgram() {
        gameCanvas.getCtx3d().useProgram(program);
    }

    private WebGLShader createShader(int type, String code) {
        WebGLShader shader = gameCanvas.getCtx3d().createShader(type);
        gameCanvas.getCtx3d().shaderSource(shader, code);
        gameCanvas.getCtx3d().compileShader(shader);
        if (!Boolean.valueOf(gameCanvas.getCtx3d().getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS).toString())) {
            String shaderType;
            if(type == WebGLRenderingContext.VERTEX_SHADER) {
                shaderType = "Vertex shader";
            } else if(type == WebGLRenderingContext.FRAGMENT_SHADER) {
                shaderType = "Fragment shader";
            } else {
                shaderType = "Unknown shader";
            }
            throw new IllegalArgumentException(shaderType + " compilation failed: " + gameCanvas.getCtx3d().getShaderInfoLog(shader));
        }
        return shader;
    }

    private WebGLProgram createAndUseProgram(WebGLShader vertexShader, WebGLShader fragmentShader) {
        WebGLProgram program = gameCanvas.getCtx3d().createProgram();
        gameCanvas.getCtx3d().attachShader(program, vertexShader);
        gameCanvas.getCtx3d().attachShader(program, fragmentShader);
        gameCanvas.getCtx3d().linkProgram(program);
        if (!Boolean.valueOf(gameCanvas.getCtx3d().getProgramParameter(program, WebGLRenderingContext.LINK_STATUS).toString())) {
            throw new IllegalArgumentException("Shader compilation failed: " + gameCanvas.getCtx3d().getProgramInfoLog(program));
        }
        gameCanvas.getCtx3d().useProgram(program);
        return program;
    }

    public void destroy() {
        gameCanvas.getCtx3d().deleteShader(vs);
        WebGlUtil.checkLastWebGlError("deleteShader vs", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().deleteShader(fs);
        WebGlUtil.checkLastWebGlError("deleteShader fs", gameCanvas.getCtx3d());
        gameCanvas.getCtx3d().deleteProgram(program);
        WebGlUtil.checkLastWebGlError("deleteProgram", gameCanvas.getCtx3d());
        program = null;
    }

}
