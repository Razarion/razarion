package com.btxtech.client.renderer.engine;

import elemental.html.WebGLRenderingContext;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Beat
 * 27.05.2016.
 */
public class TextureIdHandlerTest {

    @Test
    public void testCreate() throws Exception {
        TextureIdHandler textureIdHandler = new TextureIdHandler();
        assertTextureId(WebGLRenderingContext.TEXTURE0, 0, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE1, 1, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE2, 2, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE3, 3, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE4, 4, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE5, 5, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE6, 6, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE7, 7, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE8, 8, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE9, 9, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE10, 10, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE11, 11, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE12, 12, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE13, 13, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE14, 14, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE15, 15, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE16, 16, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE17, 17, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE18, 18, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE19, 19, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE20, 20, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE21, 21, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE22, 22, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE23, 23, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE24, 24, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE25, 25, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE26, 26, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE27, 27, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE28, 28, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE29, 29, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE30, 30, textureIdHandler.create());
        assertTextureId(WebGLRenderingContext.TEXTURE31, 31, textureIdHandler.create());

        try {
            textureIdHandler.create();
            Assert.fail("IllegalStateException expected");
        } catch(IllegalStateException e) {
            Assert.assertEquals("Running out of WebGlTextureId", e.getMessage());
        }
    }

    private void assertTextureId(int expectedWebGLId, int expectedUniformId, TextureIdHandler.WebGlTextureId actual) {
        Assert.assertEquals(expectedWebGLId, actual.getWebGlTextureId());
        Assert.assertEquals(expectedUniformId, actual.getUniformValue());
    }
}