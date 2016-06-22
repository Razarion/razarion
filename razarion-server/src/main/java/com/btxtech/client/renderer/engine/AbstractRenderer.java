package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.ShadowUiService;
import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.imageservice.ImageLoader;
import com.btxtech.client.terrain.slope.Mesh;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
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
public abstract class AbstractRenderer implements Renderer {
    // private Logger logger = Logger.getLogger(AbstractRenderer.class.getName());
    private WebGlProgram webGlProgram;
    @Inject
    private Instance<WebGlProgram> webGlProgramInstance;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private RenderService renderService;
    @Inject
    private ImageUiService imageUiService;
    private int id;
    private TextureIdHandler textureIdHandler = new TextureIdHandler();
    private TextureIdHandler.WebGlTextureId shadowWebGlTextureId;
    private int elementCount;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean hasElements() {
        return elementCount > 0;
    }

    protected void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }

    protected void setElementCount(VertexContainer vertexContainer) {
        elementCount = vertexContainer.getVerticesCount();
    }

    protected void setElementCount(VertexList vertexList) {
        elementCount = vertexList.getVerticesCount();
    }

    protected void setElementCount(Mesh mesh) {
        elementCount = mesh.size();
    }

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
        return webGlProgram.getAndEnableAttributeLocation(attributeName);
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

    protected WebGlUniformTexture createWebGLTexture(ImageDescriptor imageDescriptor, String samplerUniformName) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(), this, setupTexture(imageDescriptor), samplerUniformName, textureIdHandler.create());
    }

    protected WebGlUniformTexture createWebGLTexture(int imageId, String samplerUniformName) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(), this, setupTexture(imageId), samplerUniformName, textureIdHandler.create());
    }

    protected WebGlUniformTexture createWebGLBumpMapTexture(ImageDescriptor imageDescriptor, String samplerUniformName) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(), this, setupTextureForBumpMap(imageDescriptor), samplerUniformName, textureIdHandler.create());
    }

    protected TextureIdHandler.WebGlTextureId createWebGlTextureId() {
        return textureIdHandler.create();
    }

    protected void setLightUniforms(String postfix, LightConfig lightConfig) {
        if (postfix == null) {
            postfix = "";
        }
        uniform3f("uLightDirection" + postfix, lightConfig.getDirection());
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
        imageUiService.requestImage(imageId, new ImageUiService.ImageListener() {
            @Override
            public void onLoaded(ImageElement imageElement) {
                bindTexture(imageElement, webGLTexture);
            }
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

                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
                gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
                gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
            }
        });
        return webGLTexture;
    }

    protected WebGLRenderingContext getCtx3d() {
        return gameCanvas.getCtx3d();
    }

    protected void enableShadow() {
        shadowWebGlTextureId = createWebGlTextureId();
    }

    protected void activateShadow() {
        if (shadowWebGlTextureId == null) {
            throw new IllegalStateException("Shadow must be enabled before");
        }
        uniformMatrix4fv("uShadowMatrix", shadowUiService.createShadowLookupTransformation());
        uniform1f("uShadowAlpha", (float) shadowUiService.getShadowAlpha());
        uniform1i("uShadowTexture", shadowWebGlTextureId.getUniformValue());
        gameCanvas.getCtx3d().activeTexture(shadowWebGlTextureId.getWebGlTextureId());
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
    }

    protected void drawArrays(int mode, int count) {
        getCtx3d().drawArrays(mode, 0, count);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }

    protected void drawArrays(int mode) {
        getCtx3d().drawArrays(mode, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
