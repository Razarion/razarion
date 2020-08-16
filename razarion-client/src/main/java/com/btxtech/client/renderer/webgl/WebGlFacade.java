package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.engine.TextureIdHandler;
import com.btxtech.client.renderer.engine.WebGlPhongMaterial;
import com.btxtech.client.renderer.engine.WebGlSlopeSplatting;
import com.btxtech.client.renderer.engine.WebGlSplatting;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.DecimalPositionShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.IntegerShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.GroundSplattingConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.ViewService;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WebGlFacade {
    // private Logger logger = Logger.getLogger(WebGlFacade.class.getName());
    // Attributes
    public static final String A_VERTEX_POSITION = "position";
    public static final String A_VERTEX_NORMAL = "objectNormal";
    public static final String A_VERTEX_UV = "uv";
    @Deprecated
    public static final String A_TEXTURE_COORDINATE = "aTextureCoord";
    // Uniform common
    public static final String U_COLOR = "uColor";
    // Uniform model matrix
    public static final String U_VIEW_MATRIX = "viewMatrix";
    public static final String U_PROJECTION_MATRIX = "projectionMatrix";
    public static final String U_VIEW_NORM_MATRIX = "normalMatrix";
    public static final String U_MODEL_MATRIX = "modelMatrix";
    public static final String U_SHADOW_MATRIX = "shadowMatrix";
    // Unifrom Editor
    public static final String U_CURSOR_TYPE = "uCursorType";

    // private Logger logger = Logger.getLogger(AbstractWebGlUnitRenderer.class.getName());
    private WebGlProgram webGlProgram;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ViewService viewService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private WebGlProgramService webGlProgramService;
    @Inject
    private WebGLTextureContainer textureContainer;
    private AbstractRenderUnit abstractRenderUnit;
    private WebGlFacadeConfig webGlFacadeConfig;
    private TextureIdHandler textureIdHandler = new TextureIdHandler();
    private TextureIdHandler.WebGlTextureId shadowWebGlTextureId;
    private WebGLUniformLocation viewMatrixUniformLocation;
    private WebGLUniformLocation viewNormMatrixUniformLocation;
    private WebGLUniformLocation perspectiveMatrixUniformLocation;
    private WebGLUniformLocation receiveShadowMatrixUniformLocation;
    private WebGLUniformLocation uShadowAlpha;
    private WebGLUniformLocation uShadowTexture;


    public void init(WebGlFacadeConfig webGlFacadeConfig) {
        this.webGlFacadeConfig = webGlFacadeConfig;
        abstractRenderUnit = webGlFacadeConfig.getAbstractRenderUnit();
        webGlProgram = webGlProgramService.getWebGlProgram(webGlFacadeConfig);
        if (webGlFacadeConfig.isTransformation()) {
            if (webGlFacadeConfig.isNormTransformation()) {
                viewMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
                viewNormMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_NORM_MATRIX);
                perspectiveMatrixUniformLocation = getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
            } else {
                viewMatrixUniformLocation = getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
                perspectiveMatrixUniformLocation = getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
            }
        }
        if (webGlFacadeConfig.isReceiveShadow()) {
            receiveShadowMatrixUniformLocation = getUniformLocation(WebGlFacade.U_SHADOW_MATRIX);
            shadowWebGlTextureId = createWebGlTextureId();
            uShadowAlpha = getUniformLocation("uShadowAlpha");
            uShadowTexture = getUniformLocation("uDepthTexture");
        }
    }

    public VertexShaderAttribute createVertexShaderAttribute(String attributeName) {
        return new VertexShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public Float32ArrayShaderAttribute createFloat32ArrayShaderAttribute(String attributeName) {
        return new Float32ArrayShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public Vec3Float32ArrayShaderAttribute createVec3Float32ArrayShaderAttribute(String attributeName) {
        return new Vec3Float32ArrayShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public Vec2Float32ArrayShaderAttribute createVec2Float32ArrayShaderAttribute(String attributeName) {
        return new Vec2Float32ArrayShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public DecimalPositionShaderAttribute createDecimalPositionShaderAttribute(String attributeName) {
        return new DecimalPositionShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public FloatShaderAttribute createFloatShaderAttribute(String attributeName) {
        return new FloatShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected IntegerShaderAttribute createIntegerShaderAttribute(String attributeName) {
        return new IntegerShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public ShaderTextureCoordinateAttribute createShaderTextureCoordinateAttribute(String attributeName) {
        return new ShaderTextureCoordinateAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected double getAndEnableAttributeLocation(String attributeName) {
        return webGlProgram.getAttributeLocation(attributeName);
    }

    public WebGLUniformLocation getUniformLocation(String uniformName) {
        return webGlProgram.getUniformLocation(uniformName);
    }

    public WebGLUniformLocation getUniformLocationAlarm(String uniformName) {
        return webGlProgram.getUniformLocationAlarm(uniformName);
    }

    public void useProgram() {
        webGlProgram.useProgram();
    }

    @Deprecated
    // Do not use anymore -> slow. User: uniformMatrix4fv(String uniformName, Matrix4 matrix)
    public void uniformMatrix4fv(WebGLUniformLocation uniformLocation, Matrix4 matrix) {
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.doublesToFloat32Array(matrix.toWebGlArray()));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    public void uniformMatrix4fv(WebGLUniformLocation uniformLocation, NativeMatrix matrix) {
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.toFloat32Array(matrix));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    public void uniform3f(WebGLUniformLocation webGLUniformLocation, Vertex vertex) {
        if (webGLUniformLocation == null) {
            return;
        }
        gameCanvas.getCtx3d().uniform3f(webGLUniformLocation, (float) vertex.getX(), (float) vertex.getY(), (float) vertex.getZ());
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform3fNoAlpha(WebGLUniformLocation uniformLocation, Color color) {
        if (uniformLocation == null) {
            return;
        }
        gameCanvas.getCtx3d().uniform3f(uniformLocation, (float) color.getR(), (float) color.getG(), (float) color.getB());
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform1f(WebGLUniformLocation uniformLocation, double value) {
        gameCanvas.getCtx3d().uniform1f(uniformLocation, (float) value);
        WebGlUtil.checkLastWebGlError("uniform1f", gameCanvas.getCtx3d());
    }

    public void uniform1i(WebGLUniformLocation uniformLocation, int value) {
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value);
        WebGlUtil.checkLastWebGlError("uniform1i", gameCanvas.getCtx3d());
    }

    public void uniform1b(WebGLUniformLocation uniformLocation, boolean value) {
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value ? 1 : 0);
        WebGlUtil.checkLastWebGlError("uniform1b", gameCanvas.getCtx3d());
    }

    public void uniform4f(WebGLUniformLocation uniformLocation, double x, double y, double z, double w) {
        gameCanvas.getCtx3d().uniform4f(uniformLocation, (float) x, (float) y, (float) z, (float) w);
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform4f(WebGLUniformLocation uniformLocation, Color color) {
        uniform4f(uniformLocation, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public void uniform4f(WebGLUniformLocation uniformLocation, Vertex4 vertex4) {
        uniform4f(uniformLocation, vertex4.getX(), vertex4.getY(), vertex4.getZ(), vertex4.getW());
    }

    public WebGlUniformTexture createWebGLTexture(int imageId, String samplerUniformName) {
        return createWebGLTexture(imageId, samplerUniformName, null, null);
    }

    public WebGlUniformTexture createWebGLBumpMapTexture(int imageId, String samplerUniformName) {
        return createWebGLBumpMapTexture(imageId, samplerUniformName, null, null, null);
    }

    public WebGlUniformTexture createEmptyWebGLTexture(String samplerUniformName) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(), this, samplerUniformName, textureIdHandler.create(), null, null, null);
    }

    public WebGlUniformTexture createWebGLTexture(int imageId, String samplerUniformName, String scaleUniformLocation, Double scale) {
        WebGlUniformTexture webGlUniformTexture = new WebGlUniformTexture(gameCanvas.getCtx3d(), this, samplerUniformName, textureIdHandler.create(), scaleUniformLocation, scale, null);
        webGlUniformTexture.setWebGLTexture(textureContainer.getTexture(imageId));
        return webGlUniformTexture;
    }

    public WebGlUniformTexture createWebGLBumpMapTexture(int imageId, String samplerUniformName, String scaleUniformLocation, Double scale, String onePixelUniformLocation) {
        WebGlUniformTexture webGlUniformTexture = new WebGlUniformTexture(gameCanvas.getCtx3d(), this, samplerUniformName, textureIdHandler.create(), scaleUniformLocation, scale, onePixelUniformLocation);
        webGlUniformTexture.setWebGLTexture(textureContainer.getTextureForBumpMap(imageId));
        textureContainer.handleImageSize(imageId, webGlUniformTexture::onImageSizeReceived);
        return webGlUniformTexture;
    }

    public WebGlUniformTexture createTerrainMarkerWebGLTexture(String samplerUniformName) {
        WebGlUniformTexture webGlUniformTexture = new WebGlUniformTexture(gameCanvas.getCtx3d(), this, samplerUniformName, textureIdHandler.create(), null, null, null);
        webGlUniformTexture.setWebGLTexture(textureContainer.getTerrainMarkerTexture());
        return webGlUniformTexture;
    }

    public WebGlPhongMaterial createPhongMaterial(PhongMaterialConfig phongMaterialConfig, String variableName) {
        if (phongMaterialConfig != null) {
            return new WebGlPhongMaterial(this, phongMaterialConfig, variableName);
        } else {
            return null;
        }
    }

    public WebGlSplatting createSplatting(GroundSplattingConfig splatting, String variableName) {
        return new WebGlSplatting(this, splatting, variableName);
    }

    public WebGlSlopeSplatting createSlopeSplatting(SlopeSplattingConfig splatting, String variableName) {
        return new WebGlSlopeSplatting(this, splatting, variableName);
    }

    public WebGlUniformTexture createFakeWebGLTexture(String samplerUniformName) {
        WebGlUniformTexture webGlUniformTexture = new WebGlUniformTexture(gameCanvas.getCtx3d(), this, samplerUniformName, textureIdHandler.create(), null, null, null);
        webGlUniformTexture.setWebGLTexture(textureContainer.getFakeTexture());
        return webGlUniformTexture;
    }

    public WebGlUniformTexture createSaveWebGLTexture(Integer imageId, String samplerUniformName) {
        if (imageId != null) {
            return createWebGLTexture(imageId, samplerUniformName);
        } else {
            return createFakeWebGLTexture(samplerUniformName);
        }
    }

    private TextureIdHandler.WebGlTextureId createWebGlTextureId() {
        return textureIdHandler.create();
    }

    public WebGLRenderingContext getCtx3d() {
        return gameCanvas.getCtx3d();
    }

    public void activateReceiveShadow() {
        if (shadowWebGlTextureId == null || renderService.getPass() == RenderService.Pass.SHADOW) {
            return;
        }
        uniform1f(uShadowAlpha, (float) visualUiService.getPlanetVisualConfig().getShadowAlpha());
        uniform1i(uShadowTexture, shadowWebGlTextureId.getUniformValue());
        gameCanvas.getCtx3d().activeTexture(shadowWebGlTextureId.getWebGlTextureId());
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
    }

    public void drawArrays(double mode) {
        getCtx3d().drawArrays(mode, 0, abstractRenderUnit.getElementCount());
        WebGlUtil.checkLastWebGlError("drawArrays for " + abstractRenderUnit.helperString(), getCtx3d());
    }

    public void enableOESStandartDerivatives() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
    }

    public VisualUiService getVisualUiService() {
        return visualUiService;
    }

    public void setTransformationUniforms() {
        switch (renderService.getPass()) {
            case MAIN:
                if (viewMatrixUniformLocation != null) {
                    gameCanvas.getCtx3d().uniformMatrix4fv(viewMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewService.getViewMatrix()));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", gameCanvas.getCtx3d());
                }
                if (viewNormMatrixUniformLocation != null) {
                    gameCanvas.getCtx3d().uniformMatrix4fv(viewNormMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewService.getViewNormMatrix()));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_NORM_MATRIX", gameCanvas.getCtx3d());
                }
                if (perspectiveMatrixUniformLocation != null) {
                    // Perspective
                    gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewService.getPerspectiveMatrix()));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", gameCanvas.getCtx3d());
                }
                if(receiveShadowMatrixUniformLocation != null) {
                    gameCanvas.getCtx3d().uniformMatrix4fv(receiveShadowMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewService.getShadowLookupMatrix()));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv uShadowMatrix", gameCanvas.getCtx3d());
                }
                break;
            case SHADOW:
                if (!webGlFacadeConfig.isCastShadow()) {
                    return;
                }
                if (viewMatrixUniformLocation != null) {
                    gameCanvas.getCtx3d().uniformMatrix4fv(viewMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewService.getViewShadowMatrix()));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", gameCanvas.getCtx3d());
                }
                if (perspectiveMatrixUniformLocation != null) {
                    // Perspective
                    gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveMatrixUniformLocation, false, WebGlUtil.toFloat32Array(viewService.getPerspectiveShadowMatrix()));
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", gameCanvas.getCtx3d());
                }
                break;
            default:
                throw new IllegalStateException("Dont know how to setup transformation uniforms for render pass: " + renderService.getPass());
        }
    }
}
