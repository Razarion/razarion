package com.btxtech.client.math3d;

import com.google.gwt.core.client.GWT;
import elemental.html.WebGLProgram;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLShader;
import elemental.html.WebGLUniformLocation;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class WebGlProgram {
    private WebGLRenderingContext ctx3d;
    private WebGLProgram program;
    private WebGLShader vs;
    private WebGLShader fs;

    public WebGlProgram(WebGLRenderingContext ctx3d) {
        this.ctx3d = ctx3d;
    }

    public void createProgram(String vertexShaderCode, String fragmentShaderCode) {
        vs = createShader(WebGLRenderingContext.VERTEX_SHADER, vertexShaderCode);
        fs = createShader(WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderCode);
        program = createAndUseProgram(vs, fs);
    }

    public int getAndEnableAttributeLocation(String attributeName) {
        int attributeLocation = ctx3d.getAttribLocation(program, attributeName);
        if (attributeLocation == -1) {
            throw new IllegalArgumentException("No attribute location for '" + attributeName + "' in OpenGl program.");
        }
        ctx3d.enableVertexAttribArray(attributeLocation);
        return attributeLocation;
    }

    public WebGLUniformLocation getUniformLocation(String uniformName) {
        WebGLUniformLocation uniform = ctx3d.getUniformLocation(program, uniformName);
        if (uniform == null) {
            throw new IllegalArgumentException("No uniform location for '" + uniformName + "' in OpenGl program.");
        }
        return uniform;
    }

    public void useProgram() {
        ctx3d.useProgram(program);
    }

    private WebGLShader createShader(int type, String code) {
        WebGLShader shader = ctx3d.createShader(type);
        ctx3d.shaderSource(shader, code);
        ctx3d.compileShader(shader);
        if (!Boolean.valueOf(ctx3d.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS).toString())) {
            throw new IllegalArgumentException("Shader compilation failed: " + ctx3d.getShaderInfoLog(shader));
        }
        return shader;
    }

    private WebGLProgram createAndUseProgram(WebGLShader vertexShader, WebGLShader fragmentShader) {
        WebGLProgram program = ctx3d.createProgram();
        ctx3d.attachShader(program, vertexShader);
        ctx3d.attachShader(program, fragmentShader);
        ctx3d.linkProgram(program);
        if (!Boolean.valueOf(ctx3d.getProgramParameter(program, WebGLRenderingContext.LINK_STATUS).toString())) {
            throw new IllegalArgumentException("Shader compilation failed: " + ctx3d.getProgramInfoLog(program));
        }
        ctx3d.useProgram(program);
        return program;
    }

    public void destroy() {
        ctx3d.deleteShader(vs);
        WebGlUtil.checkLastWebGlError("deleteShader vs", ctx3d);
        ctx3d.deleteShader(fs);
        WebGlUtil.checkLastWebGlError("deleteShader fs", ctx3d);
        ctx3d.deleteProgram(program);
        WebGlUtil.checkLastWebGlError("deleteProgram", ctx3d);
        program = null;
    }

}
