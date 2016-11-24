package com.btxtech.client.renderer.engine;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.imageservice.ImageLoader;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.TextResource;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Deprecated
public abstract class AbstractWebGlUnitRenderer extends AbstractRenderUnit {
    // Attributes
    String A_VERTEX_POSITION = "aVertexPosition";
    String A_VERTEX_NORMAL = "aVertexNormal";
    String A_VERTEX_TANGENT = "aVertexTangent";
    String A_TEXTURE_COORDINATE = "aTextureCoord";
    String A_BARYCENTRIC = "aBarycentric";
    String A_GROUND_SPLATTING = "aGroundSplatting";
    // Uniform model matrix
    String U_PERSPECTIVE_MATRIX = "uPMatrix";
    String U_VIEW_MATRIX = "uVMatrix";
    String U_VIEW_NORM_MATRIX = "uNVMatrix";
    String U_MODEL_MATRIX = "uMMatrix";
    String U_MODEL_NORM_MATRIX = "uNMatrix";
    // Uniform Light
    String U_LIGHT_DIRECTION = "uLightDirection";
    String U_LIGHT_DIFFUSE= "uLightDiffuse";
    String U_LIGHT_AMBIENT = "uLightAmbient";
    String U_LIGHT_SPECULAR_INTENSITY = "uLightSpecularIntensity";
    String U_LIGHT_SPECULAR_HARDNESS = "uLightSpecularHardness";
    // Unifrom Editor
    String U_CURSOR_TYPE = "uCursorType";

    // private Logger logger = Logger.getLogger(AbstractWebGlUnitRenderer.class.getName());
    private WebGlProgram webGlProgram;
    @Inject
    private Instance<WebGlProgram> webGlProgramInstance;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private ImageUiService imageUiService;
    private TextureIdHandler textureIdHandler = new TextureIdHandler();
    private TextureIdHandler.WebGlTextureId shadowWebGlTextureId;

    protected void createProgram(TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        webGlProgram = webGlProgramInstance.get();
        webGlProgram.createProgram(vertexShaderCode.getText(), fragmentShaderCode.getText());
    }

    protected VertexShaderAttribute createVertexShaderAttribute(String attributeName) {
        return new VertexShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected FloatShaderAttribute createFloatShaderAttribute(String attributeName) {
        return new FloatShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected IntegerShaderAttribute createIntegerShaderAttribute(String attributeName) {
        return new IntegerShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected ShaderTextureCoordinateAttribute createShaderTextureCoordinateAttributee(String attributeName) {
        return new ShaderTextureCoordinateAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected int getAndEnableAttributeLocation(String attributeName) {
        return webGlProgram.getAttributeLocation(attributeName);
    }

    protected WebGLUniformLocation getUniformLocation(String uniformName) {
        return webGlProgram.getUniformLocation(uniformName);
    }

    protected void useProgram() {
        webGlProgram.useProgram();
    }

    protected void uniformMatrix4fv(String uniformName, Matrix4 matrix) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.createArrayBufferOfFloat32Doubles(matrix.toWebGlArray()));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    protected void uniform3f(String uniformName, double x, double y, double z) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform3f(uniformLocation, (float) x, (float) y, (float) z);
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    protected void uniform3f(String uniformName, Vertex vertex) {
        uniform3f(uniformName, vertex.getX(), vertex.getY(), vertex.getZ());
    }

    protected void uniform3fNoAlpha(String uniformName, Color color) {
        uniform3f(uniformName, color.getR(), color.getG(), color.getB());
    }

    protected void uniform1f(String uniformName, double value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1f(uniformLocation, (float) value);
        WebGlUtil.checkLastWebGlError("uniform1f", gameCanvas.getCtx3d());
    }

    protected void uniform1i(String uniformName, int value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value);
        WebGlUtil.checkLastWebGlError("uniform1i", gameCanvas.getCtx3d());
    }

    protected void uniform1b(String uniformName, boolean value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value ? 1 : 0);
        WebGlUtil.checkLastWebGlError("uniform1b", gameCanvas.getCtx3d());
    }

    protected WebGlUniformTexture_OLD createWebGLTexture(ImageDescriptor imageDescriptor, String samplerUniformName) {
        return new WebGlUniformTexture_OLD(gameCanvas.getCtx3d(), this, setupTexture(imageDescriptor), samplerUniformName, textureIdHandler.create());
    }

    protected WebGlUniformTexture_OLD createWebGLTexture(int imageId, String samplerUniformName) {
        return new WebGlUniformTexture_OLD(gameCanvas.getCtx3d(), this, setupTexture(imageId), samplerUniformName, textureIdHandler.create());
    }

    protected WebGlUniformTexture_OLD createWebGLBumpMapTexture(ImageDescriptor imageDescriptor, String samplerUniformName) {
        return new WebGlUniformTexture_OLD(gameCanvas.getCtx3d(), this, setupTextureForBumpMap(imageDescriptor), samplerUniformName, textureIdHandler.create());
    }

    protected WebGlUniformTexture_OLD createWebGLBumpMapTexture(int imageId, String samplerUniformName) {
        return new WebGlUniformTexture_OLD(gameCanvas.getCtx3d(), this, setupTextureForBumpMap(imageId), samplerUniformName, textureIdHandler.create());
    }

    protected TextureIdHandler.WebGlTextureId createWebGlTextureId() {
        return textureIdHandler.create();
    }

    protected void setLightUniforms(String postfix, LightConfig lightConfig) {
        if (postfix == null) {
            postfix = "";
        }
        uniform3f("uLightDirection" + postfix, lightConfig.setupDirection());
        uniform3fNoAlpha("uLightDiffuse" + postfix, lightConfig.getDiffuse());
        uniform3fNoAlpha("uLightAmbient" + postfix, lightConfig.getAmbient());
        uniform1f("uLightSpecularIntensity" + postfix, lightConfig.getSpecularIntensity());
        uniform1f("uLightSpecularHardness" + postfix, lightConfig.getSpecularHardness());
    }

    protected WebGLTexture setupTexture(final ImageDescriptor imageDescriptor) {
        final WebGLTexture webGLTexture = gameCanvas.getCtx3d().createTexture();
        ImageLoader<WebGLTexture> textureLoader = new ImageLoader<>();
        textureLoader.addImageUrl(imageDescriptor.getUrl(), webGLTexture);
        textureLoader.startLoading(new ImageLoader.Listener<WebGLTexture>() {
            @Override
            public void onLoaded(Map<WebGLTexture, ImageElement> loadedImageElements, Collection<WebGLTexture> failed) {
                if (!failed.isEmpty()) {
                    throw new IllegalStateException("Failed loading texture: " + imageDescriptor.getUrl());
                }
                ImageElement imageElement = loadedImageElements.get(webGLTexture);
                if (imageElement == null) {
                    throw new IllegalStateException("Failed loading texture: " + imageDescriptor.getUrl());
                }

                bindTexture(imageElement, webGLTexture);
            }
        });
        return webGLTexture;
    }

    protected WebGLTexture setupTexture(int imageId) {
        final WebGLTexture webGLTexture = gameCanvas.getCtx3d().createTexture();
        imageUiService.requestImage(imageId, imageElement -> {
            bindTexture(imageElement, webGLTexture);
        });
        return webGLTexture;
    }

    private void bindTexture(ImageElement imageElement, WebGLTexture webGLTexture) {
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR_MIPMAP_NEAREST);
        gameCanvas.getCtx3d().generateMipmap(WebGLRenderingContext.TEXTURE_2D);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }

    protected WebGLTexture setupTextureForBumpMap(ImageDescriptor imageDescriptor) {
        final WebGLTexture webGLTexture = gameCanvas.getCtx3d().createTexture();
        ImageLoader<WebGLTexture> textureLoader = new ImageLoader<>();
        textureLoader.addImageUrl(imageDescriptor.getUrl(), webGLTexture);
        textureLoader.startLoading(new ImageLoader.Listener<WebGLTexture>() {
            @Override
            public void onLoaded(Map<WebGLTexture, ImageElement> loadedImageElements, Collection<WebGLTexture> failed) {
                if (!failed.isEmpty()) {
                    throw new IllegalStateException("Failed loading texture");
                }
                ImageElement imageElement = loadedImageElements.get(webGLTexture);
                if (imageElement == null) {
                    throw new IllegalStateException("Failed loading texture");
                }

                bindTextureForBumpMap(imageElement, webGLTexture);
            }
        });
        return webGLTexture;
    }

    protected WebGLTexture setupTextureForBumpMap(int imageId) {
        final WebGLTexture webGLTexture = gameCanvas.getCtx3d().createTexture();
        imageUiService.requestImage(imageId, imageElement -> {
            bindTextureForBumpMap(imageElement, webGLTexture);
        });
        return webGLTexture;
    }

    private void bindTextureForBumpMap(ImageElement imageElement, WebGLTexture webGLTexture) {
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }

    protected WebGLRenderingContext getCtx3d() {
        return gameCanvas.getCtx3d();
    }

    protected void enableShadow() {
        shadowWebGlTextureId = createWebGlTextureId();
    }

    protected void drawArrays(int mode, int count) {
        getCtx3d().drawArrays(mode, 0, count);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }

    protected void drawArrays(int mode) {
        getCtx3d().drawArrays(mode, 0, getElementCount());
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(Object o) {

    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {

    }
}
