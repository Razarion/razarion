package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.VertexList;
import com.btxtech.game.jsre.common.ImageLoader;
import com.google.gwt.dom.client.ImageElement;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

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

    protected void createProgram(WebGLRenderingContext ctx3d, String vertexShaderCode, String fragmentShaderCode) {
        webGlProgram = new WebGlProgram(ctx3d);
        webGlProgram.createProgram(vertexShaderCode, fragmentShaderCode);
        vertexPositionAttribute = webGlProgram.getAndEnableAttributeLocation(A_VERTEX_POSITION);
        verticesBuffer = ctx3d.createBuffer();
    }

    protected abstract void customDraw(WebGLRenderingContext ctx3d, Lighting lighting);

    protected abstract void fillCustomBuffers(WebGLRenderingContext ctx3d, VertexList vertexList);

    public void fillBuffers(WebGLRenderingContext ctx3d, VertexList vertexList ) {
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        ctx3d.bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);

        fillCustomBuffers(ctx3d, vertexList);

        elementCount = vertexList.getVerticesCount();
    }

    public void draw(WebGLRenderingContext ctx3d, ProjectionTransformation projectionTransformation, ModelTransformation modelTransformation, ViewTransformation viewTransformation, Lighting lighting) {
        webGlProgram.useProgram();
        // Projection uniform
        WebGLUniformLocation pUniform = webGlProgram.getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        ctx3d.uniformMatrix4fv(pUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // Model model transformation uniform
        WebGLUniformLocation mVUniform = webGlProgram.getUniformLocation(MODEL_VIEW_UNIFORM_NAME);
        ctx3d.uniformMatrix4fv(mVUniform, false, WebGlUtil.createArrayBufferOfFloat32(viewTransformation.createMatrix().multiply(modelTransformation.createMatrix()).toWebGlArray()));
        // set vertices position
        ctx3d.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        ctx3d.vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);

        customDraw(ctx3d, lighting);

        ctx3d.drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", ctx3d);
    }

    public void destroy() {
        webGlProgram.useProgram();
        webGlProgram.destroy();
        webGlProgram = null;
    }

    protected WebGlProgram getWebGlProgram() {
        return webGlProgram;
    }

    protected WebGLTexture setupTexture(final WebGLRenderingContext ctx3d, ImageDescriptor imageDescriptor) {
        final WebGLTexture webGLTexture = ctx3d.createTexture();
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

                ctx3d.bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
                ctx3d.pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
                ctx3d.texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) WebGlUtil.castElementToElement(imageElement));
                ctx3d.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
                ctx3d.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.NEAREST);
                ctx3d.bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
            }
        });
        return webGLTexture;
    }

}
