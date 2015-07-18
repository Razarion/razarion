package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.shaders.Shaders;

/**
 * Created by Beat
 * 20.05.2015.
 */
public class TriangleRenderHolder {
    private static final ImageDescriptor CHESS_TEXTURE_32 = new ImageDescriptor("chess32.jpg", 512, 512);
    private static final ImageDescriptor CHESS_TEXTURE_08 = new ImageDescriptor("chess08.jpg", 512, 512);
    private AbstractTriangleRenderUnit triangleRenderUnit;
    private final ImageDescriptor imageDescriptor;
    private ImageDescriptor currentImageDescriptor;
    private final VertexListProvider vertexListProvider;

    public TriangleRenderHolder(TriangleRenderManager triangleRenderManager, TriangleRenderManager.Mode mode, VertexListProvider vertexListProvider, ImageDescriptor imageDescriptor) {
        this.vertexListProvider = vertexListProvider;
        this.imageDescriptor = imageDescriptor;
        createTriangleRenderUnit(triangleRenderManager, mode);
    }

    public void createTriangleRenderUnit(TriangleRenderManager manager, TriangleRenderManager.Mode mode) {
        if (triangleRenderUnit != null) {
            triangleRenderUnit.destroy();
        }
        switch (mode) {
            case NORMAL:
                triangleRenderUnit = createTextureRenderer(manager, imageDescriptor);
                break;
            case LIGHT:
                triangleRenderUnit = createLightRenderer(manager);
                break;
            case WIRE_08:
                triangleRenderUnit = createWireRenderer(manager, CHESS_TEXTURE_08);
                break;
            case WIRE_32:
                triangleRenderUnit = createWireRenderer(manager, CHESS_TEXTURE_32);
                break;
            case DEV_TEX_08:
                triangleRenderUnit = createTextureRenderer(manager, CHESS_TEXTURE_08);
                break;
            case DEV_TEX_32:
                triangleRenderUnit = createTextureRenderer(manager, CHESS_TEXTURE_32);
                break;
        }
    }

    public void fillBuffers(TriangleRenderManager manager) {
        triangleRenderUnit.fillBuffers(manager.getCtx3d(), vertexListProvider.provideVertexList(currentImageDescriptor));
    }

    public void draw(TriangleRenderManager manager) {
        triangleRenderUnit.draw(manager.getCtx3d(), manager.getProjectionTransformation(), manager.getModelTransformation(), manager.getViewTransformation(), manager.getLighting());
    }

    private NormalTriangleRenderUnit createTextureRenderer(TriangleRenderManager manager, ImageDescriptor imageDescriptor) {
        currentImageDescriptor = imageDescriptor;
        NormalTriangleRenderUnit normalTriangleRenderUnit = new NormalTriangleRenderUnit(manager.getCtx3d(), Shaders.INSTANCE.normalVertexShader().getText(), Shaders.INSTANCE.normalFragmentShader().getText());
        normalTriangleRenderUnit.createTexture(manager.getCtx3d(), currentImageDescriptor);
        return normalTriangleRenderUnit;
    }

    private LightTriangleRenderUnit createLightRenderer(TriangleRenderManager manager) {
        currentImageDescriptor = CHESS_TEXTURE_32; // Prevent NPE
        return new LightTriangleRenderUnit(manager.getCtx3d(), Shaders.INSTANCE.LightVertexShader().getText(), Shaders.INSTANCE.LightFragmentShader().getText(), new Color(1.0, 1.0, 1.0));
    }

    private WireTriangleRenderUnit createWireRenderer(TriangleRenderManager manager, ImageDescriptor imageDescriptor) {
        currentImageDescriptor = imageDescriptor;
        WireTriangleRenderUnit wireTriangleRenderUnit = new WireTriangleRenderUnit(manager.getCtx3d(), Shaders.INSTANCE.wireVertexShader().getText(), Shaders.INSTANCE.wireFragmentShader().getText());
        wireTriangleRenderUnit.createTexture(manager.getCtx3d(), imageDescriptor);
        return wireTriangleRenderUnit;
    }

}
