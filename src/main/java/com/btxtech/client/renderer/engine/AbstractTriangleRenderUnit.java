package com.btxtech.client.renderer.engine;

import com.btxtech.client.GameCanvas;
import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.ModelTransformation;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.ViewTransformation;
import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.VertexList;
import com.btxtech.game.jsre.common.ImageLoader;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.TextResource;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 20.05.2015.
 */
public abstract class AbstractTriangleRenderUnit {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String MODEL_VIEW_UNIFORM_NAME = "uMVMatrix";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGlProgram webGlProgram;
    private int elementCount;
    @Inject
    private Instance<WebGlProgram> webGlProgramInstance;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private ModelTransformation modelTransformation;
    @Inject
    private ProjectionTransformation projectionTransformation;

    protected void createProgram(TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        webGlProgram = webGlProgramInstance.get();
        webGlProgram.createProgram(vertexShaderCode.getText(), fragmentShaderCode.getText());
        vertexPositionAttribute = webGlProgram.getAndEnableAttributeLocation(A_VERTEX_POSITION);
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
    }

    protected abstract void customDraw();

    protected abstract void fillCustomBuffers(VertexList vertexList);

    public void fillBuffers(VertexList vertexList) {
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);

        fillCustomBuffers(vertexList);

        elementCount = vertexList.getVerticesCount();
    }

    public void draw() {
        webGlProgram.useProgram();
        // Projection uniform
        WebGLUniformLocation pUniform = webGlProgram.getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(pUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // Model model transformation uniform
        WebGLUniformLocation mVUniform = webGlProgram.getUniformLocation(MODEL_VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(mVUniform, false, WebGlUtil.createArrayBufferOfFloat32(viewTransformation.createMatrix().multiply(modelTransformation.createMatrix()).toWebGlArray()));
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);

        customDraw();

        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }

    public void destroy() {
        webGlProgram.useProgram();
        webGlProgram.destroy();
        webGlProgram = null;
    }

    protected WebGlProgram getWebGlProgram() {
        return webGlProgram;
    }

    protected WebGLTexture setupTexture(ImageDescriptor imageDescriptor) {
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
                gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) WebGlUtil.castElementToElement(imageElement));
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.NEAREST);
                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
            }
        });
        return webGLTexture;
    }

}
