package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.engine.DecimalPositionShaderAttribute;
import com.btxtech.client.renderer.engine.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.IntegerShaderAttribute;
import com.btxtech.client.renderer.engine.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.TextureIdHandler;
import com.btxtech.client.renderer.engine.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.google.gwt.resources.client.TextResource;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

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
    public static final String A_VERTEX_POSITION = "aVertexPosition";
    public static final String A_VERTEX_NORMAL = "aVertexNormal";
    public static final String A_VERTEX_TANGENT = "aVertexTangent";
    public static final String A_TEXTURE_COORDINATE = "aTextureCoord";
    public static final String A_BARYCENTRIC = "aBarycentric";
    // Attributes Terrain
    public static final String A_GROUND_SPLATTING = "aGroundSplatting";
    // Uniform common
    public static final String U_COLOR = "uColor";
    public static final String U_TEXTURE = "uTexture";
    // Uniform model matrix
    public static final String U_PERSPECTIVE_MATRIX = "uPMatrix";
    public static final String U_VIEW_MATRIX = "uVMatrix";
    public static final String U_VIEW_NORM_MATRIX = "uNVMatrix";
    public static final String U_MODEL_MATRIX = "uMMatrix";
    public static final String U_MODEL_NORM_MATRIX = "uNMatrix";
    // Uniform Light
    public static final String U_LIGHT_DIRECTION = "uLightDirection";
    public static final String U_LIGHT_DIFFUSE = "uLightDiffuse";
    public static final String U_LIGHT_AMBIENT = "uLightAmbient";
    public static final String U_LIGHT_SPECULAR_INTENSITY = "uLightSpecularIntensity";
    public static final String U_LIGHT_SPECULAR_HARDNESS = "uLightSpecularHardness";
    // Unifrom Editor
    public static final String U_CURSOR_TYPE = "uCursorType";

    // private Logger logger = Logger.getLogger(AbstractWebGlUnitRenderer.class.getName());
    private WebGlProgram webGlProgram;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private WebGlProgramService webGlProgramService;
    @Inject
    private WebGLTextureContainer textureContainer;
    private AbstractRenderUnit abstractRenderUnit;
    private TextureIdHandler textureIdHandler = new TextureIdHandler();
    private TextureIdHandler.WebGlTextureId shadowWebGlTextureId;

    public void setAbstractRenderUnit(AbstractRenderUnit abstractRenderUnit) {
        this.abstractRenderUnit = abstractRenderUnit;
    }

    public void createProgram(TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        webGlProgram = webGlProgramService.getWebGlProgram(vertexShaderCode.getText(), fragmentShaderCode.getText());
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

    protected int getAndEnableAttributeLocation(String attributeName) {
        return webGlProgram.getAttributeLocation(attributeName);
    }

    public WebGLUniformLocation getUniformLocation(String uniformName) {
        return webGlProgram.getUniformLocation(uniformName);
    }


    public void useProgram() {
        webGlProgram.useProgram();
    }

    public void uniformMatrix4fv(String uniformName, Matrix4 matrix) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.createArrayBufferOfFloat32Doubles(matrix.toWebGlArray()));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    protected void uniform3f(String uniformName, double x, double y, double z) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform3f(uniformLocation, (float) x, (float) y, (float) z);
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform3f(String uniformName, Vertex vertex) {
        uniform3f(uniformName, vertex.getX(), vertex.getY(), vertex.getZ());
    }

    public void uniform3fNoAlpha(String uniformName, Color color) {
        uniform3f(uniformName, color.getR(), color.getG(), color.getB());
    }

    public void uniform1f(String uniformName, double value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1f(uniformLocation, (float) value);
        WebGlUtil.checkLastWebGlError("uniform1f", gameCanvas.getCtx3d());
    }

    public void uniform1i(String uniformName, int value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value);
        WebGlUtil.checkLastWebGlError("uniform1i", gameCanvas.getCtx3d());
    }

    public void uniform1b(String uniformName, boolean value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value ? 1 : 0);
        WebGlUtil.checkLastWebGlError("uniform1b", gameCanvas.getCtx3d());
    }

    public void uniform4f(String uniformName, double x, double y, double z, double w) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform4f(uniformLocation, (float) x, (float) y, (float) z, (float) w);
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform4f(String uniformName, Color color) {
        uniform4f(uniformName, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public WebGlUniformTexture createWebGLTexture(int imageId, String samplerUniformName) {
        return createWebGLTexture(imageId, samplerUniformName, null, null);
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

    private TextureIdHandler.WebGlTextureId createWebGlTextureId() {
        return textureIdHandler.create();
    }

    public void setLightUniforms(LightConfig lightConfig) {
        setLightUniforms(null, lightConfig);
    }

    public void setLightUniforms(String postfix, LightConfig lightConfig) {
        if (postfix == null) {
            postfix = "";
        }
        uniform3f("uLightDirection" + postfix, lightConfig.setupDirection());
        uniform3fNoAlpha("uLightDiffuse" + postfix, lightConfig.getDiffuse());
        uniform3fNoAlpha("uLightAmbient" + postfix, lightConfig.getAmbient());
        uniform1f("uLightSpecularIntensity" + postfix, lightConfig.getSpecularIntensity());
        uniform1f("uLightSpecularHardness" + postfix, lightConfig.getSpecularHardness());
    }

    public WebGLRenderingContext getCtx3d() {
        return gameCanvas.getCtx3d();
    }

    public void enableReceiveShadow() {
        shadowWebGlTextureId = createWebGlTextureId();
    }

    public void activateReceiveShadow() {
        if (shadowWebGlTextureId == null) {
            throw new IllegalStateException("Shadow must be enabled before");
        }
        uniformMatrix4fv("uShadowMatrix", shadowUiService.getShadowLookupTransformation());
        uniform1f("uShadowAlpha", (float) shadowUiService.getShadowAlpha());
        uniform1i("uShadowTexture", shadowWebGlTextureId.getUniformValue());
        gameCanvas.getCtx3d().activeTexture(shadowWebGlTextureId.getWebGlTextureId());
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
    }

    public void drawArrays(int mode) {
        getCtx3d().drawArrays(mode, 0, abstractRenderUnit.getElementCount());
        WebGlUtil.checkLastWebGlError("drawArrays for " + abstractRenderUnit.helperString(), getCtx3d());
    }

    public void enableOESStandartDerivatives() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
    }
}
