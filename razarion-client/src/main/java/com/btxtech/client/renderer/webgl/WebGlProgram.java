package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.renderer.ViewService;
import elemental2.webgl.WebGLProgram;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLShader;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.04.2015.
 */
@Dependent
public class WebGlProgram {
    // private Logger logger = Logger.getLogger(WebGlProgram.class.getName());
    private WebGLProgram program;
    private WebGLShader vs;
    private WebGLShader fs;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ViewService viewService;
    private Runnable transformUnregisterHandler;
    private Runnable shadowLookupUnregisterHandler;
    private WebGLUniformLocation viewMatrixUniformLocation;
    private WebGLUniformLocation viewNormMatrixUniformLocation;
    private WebGLUniformLocation perspectiveMatrixUniformLocation;
    private WebGLUniformLocation shadowViewMatrixUniformLocation;
    private WebGLUniformLocation shadowPerspectiveMatrixUniformLocation;
    private WebGLUniformLocation shadowMatrixUniformLocation;

    public void createProgram(WebGlFacadeConfig webGlFacadeConfig) {
        vs = createShader(WebGLRenderingContext.VERTEX_SHADER, webGlFacadeConfig.getVertexShaderCode().getText());
        fs = createShader(WebGLRenderingContext.FRAGMENT_SHADER, webGlFacadeConfig.getFragmentShaderCode().getText());
        program = createAndUseProgram(vs, fs);

        if (webGlFacadeConfig.isTransformation()) {
            if (webGlFacadeConfig.isNormTransformation()) {
                useProgram();
                viewMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
                viewNormMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_NORM_MATRIX);
                perspectiveMatrixUniformLocation = getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
                transformUnregisterHandler = viewService.addAndCallTransformationNormListener((viewMatrix, viewNormMatrix, perspectiveMatrix) -> {
                    useProgram();
                    // View
                    gameCanvas.getCtx3d().uniformMatrix4fv(viewMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewMatrix));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", gameCanvas.getCtx3d());
                    // View Norm
                    gameCanvas.getCtx3d().uniformMatrix4fv(viewNormMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewNormMatrix));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_NORM_MATRIX", gameCanvas.getCtx3d());
                    // Perspective
                    gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveMatrixUniformLocation, false, WebGlUtil.toFloat32Array(perspectiveMatrix));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", gameCanvas.getCtx3d());
                });
            } else {
                useProgram();
                viewMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
                perspectiveMatrixUniformLocation = getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
                transformUnregisterHandler = viewService.addAndCallTransformationListener((viewMatrix, perspectiveMatrix) -> {
                    useProgram();
                    // View
                    gameCanvas.getCtx3d().uniformMatrix4fv(viewMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewMatrix));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", gameCanvas.getCtx3d());
                    // Perspective
                    gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveMatrixUniformLocation, false, WebGlUtil.toFloat32Array(perspectiveMatrix));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", gameCanvas.getCtx3d());
                });
            }
        } else if (webGlFacadeConfig.isShadowTransformation()) {
            useProgram();
            shadowViewMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
            shadowPerspectiveMatrixUniformLocation = getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
            transformUnregisterHandler = viewService.addAndCallShadowTransformationListener((viewShadowMatrix, perspectiveShadowMatrix) -> {
                useProgram();
                // View
                gameCanvas.getCtx3d().uniformMatrix4fv(shadowViewMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewShadowMatrix));
                WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", gameCanvas.getCtx3d());
                // Perspective
                gameCanvas.getCtx3d().uniformMatrix4fv(shadowPerspectiveMatrixUniformLocation, false, WebGlUtil.toFloat32Array(perspectiveShadowMatrix));
                WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", gameCanvas.getCtx3d());
            });
        }

        if (webGlFacadeConfig.isReceiveShadow()) {
            useProgram();
            shadowMatrixUniformLocation = getUniformLocation(WebGlFacade.U_SHADOW_MATRIX);
            shadowLookupUnregisterHandler = viewService.addAndCallShadowLookupTransformationListener(shadowLookupMatrix -> {
                useProgram();
                gameCanvas.getCtx3d().uniformMatrix4fv(shadowMatrixUniformLocation, false, WebGlUtil.toFloat32Array(shadowLookupMatrix));
                WebGlUtil.checkLastWebGlError("uniformMatrix4fv uShadowMatrix", gameCanvas.getCtx3d());
            });
        }

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
