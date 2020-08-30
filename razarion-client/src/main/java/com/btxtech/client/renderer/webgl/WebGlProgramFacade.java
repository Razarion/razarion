package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.shaders.library.GlslLibrarian;
import com.btxtech.client.utils.DomConstants;
import com.btxtech.shared.system.alarm.AlarmService;
import elemental2.webgl.WebGLProgram;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLShader;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static com.btxtech.shared.system.alarm.Alarm.Type.RENDER_ENGINE_UNIFORM;

/**
 * Created by Beat
 * 11.04.2015.
 */
@Dependent
public class WebGlProgramFacade {
    // private Logger logger = Logger.getLogger(WebGlProgram.class.getName());
    private WebGLProgram program;
    private WebGLShader vs;
    private WebGLShader fs;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private AlarmService alarmService;
    @Inject
    private GlslLibrarian glslLibrarian;
    private Runnable transformUnregisterHandler;
    private Runnable shadowLookupUnregisterHandler;

    public void createProgram(String vertexShaderCodee, String fragmentShaderCode) {
        vs = createShader(WebGLRenderingContext.VERTEX_SHADER, vertexShaderCodee);
        fs = createShader(WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderCode);
        program = createAndUseProgram(vs, fs);
    }

    public double getAttributeLocation(String attributeName) {
        double attributeLocation = gameCanvas.getCtx3d().getAttribLocation(program, attributeName);
        if (attributeLocation == -1) {
            throw new IllegalArgumentException("No attribute location for '" + attributeName + "' in OpenGl program.");
        }
        return attributeLocation;
    }

    public WebGLUniformLocation getUniformLocation(String uniformName) {
        WebGLUniformLocation uniform = gameCanvas.getCtx3d().getUniformLocation(program, uniformName);
        if (uniform == null) {
            throw new IllegalArgumentException("No uniform location for '" + uniformName + "' in OpenGl program.");
        }
        return uniform;
    }

    public WebGLUniformLocation getUniformLocationAlarm(String uniformName) {
        WebGLUniformLocation uniform = gameCanvas.getCtx3d().getUniformLocation(program, uniformName);
        if (uniform == null) {
            alarmService.riseAlarm(RENDER_ENGINE_UNIFORM, "No uniform location for '" + uniformName + "' in OpenGl program.");
        }
        return uniform;
    }

    public void useProgram() {
        gameCanvas.getCtx3d().useProgram(program);
    }

    private WebGLShader createShader(double type, String code) {
        WebGLShader shader = gameCanvas.getCtx3d().createShader(type);
        gameCanvas.getCtx3d().shaderSource(shader, code);
        gameCanvas.getCtx3d().compileShader(shader);
        if (!Boolean.valueOf(gameCanvas.getCtx3d().getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS).toString())) {
            String shaderType;
            if (type == WebGLRenderingContext.VERTEX_SHADER) {
                shaderType = "Vertex shader";
            } else if (type == WebGLRenderingContext.FRAGMENT_SHADER) {
                shaderType = "Fragment shader";
            } else {
                shaderType = "Unknown shader";
            }
            throw new IllegalArgumentException(shaderType + " compilation failed: " + gameCanvas.getCtx3d().getShaderInfoLog(shader)
                    + "\n"
                    + addLineNumbers(code));
        }
        return shader;
    }

    private String addLineNumbers(String code) {
        StringBuilder result = new StringBuilder();
        String[] lines = code.split(DomConstants.JAVASCRIPT_LINE_SEPARATOR);
        for (int i = 0; i < lines.length; i++) {
            result.append("[");
            result.append(i + 1);
            result.append("]");
            result.append(lines[i]);
        }
        return result.toString();
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
        if (transformUnregisterHandler != null) {
            transformUnregisterHandler.run();
            transformUnregisterHandler = null;
        }
        if (shadowLookupUnregisterHandler != null) {
            shadowLookupUnregisterHandler.run();
            shadowLookupUnregisterHandler = null;
        }
    }
}
