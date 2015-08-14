package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.Terrain;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent // Why is dependent needed???
public class TriangleRenderHolder {
    private static final ImageDescriptor CHESS_TEXTURE_32 = new ImageDescriptor("chess32.jpg", 512, 512);
    private static final ImageDescriptor CHESS_TEXTURE_08 = new ImageDescriptor("chess08.jpg", 512, 512);
    private AbstractTriangleRenderUnit triangleRenderUnit;
    private ImageDescriptor imageDescriptor;
    private ImageDescriptor currentImageDescriptor;
    private VertexListProvider vertexListProvider;
    @Inject
    private Instance<AbstractTriangleRenderUnit> renderUnitsInstance;

    public void setVertexListProvider(VertexListProvider vertexListProvider) {
        this.vertexListProvider = vertexListProvider;
    }

    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    public void createTriangleRenderUnit(TriangleRenderManager.Mode mode) {
        if (triangleRenderUnit != null) {
            triangleRenderUnit.destroy();
        }
        switch (mode) {
            case NORMAL:
                triangleRenderUnit = createTextureRenderer(imageDescriptor);
                break;
            case LIGHT:
                triangleRenderUnit = createLightRenderer();
                break;
            case WIRE_08:
                triangleRenderUnit = createWireRenderer(CHESS_TEXTURE_08);
                break;
            case WIRE_32:
                triangleRenderUnit = createWireRenderer(CHESS_TEXTURE_32);
                break;
            case DEV_TEX_08:
                triangleRenderUnit = createTextureRenderer(CHESS_TEXTURE_08);
                break;
            case DEV_TEX_32:
                triangleRenderUnit = createTextureRenderer(CHESS_TEXTURE_32);
                break;
            case MULTI_TEX:
                triangleRenderUnit = createMultiTextureRenderer(Terrain.GRASS_IMAGE, Terrain.BLEND_2, Terrain.SAND_2);
                break;
        }
    }

    public void fillBuffers() {
        triangleRenderUnit.fillBuffers(vertexListProvider.provideVertexList(currentImageDescriptor));
    }

    public void draw() {
        triangleRenderUnit.draw();
    }

    private NormalTriangleRenderUnit createTextureRenderer(ImageDescriptor imageDescriptor) {
        currentImageDescriptor = imageDescriptor;
        NormalTriangleRenderUnit normalTriangleRenderUnit = renderUnitsInstance.select(NormalTriangleRenderUnit.class).get();
        normalTriangleRenderUnit.init(imageDescriptor);
        return normalTriangleRenderUnit;
    }

    private LightTriangleRenderUnit createLightRenderer() {
        currentImageDescriptor = CHESS_TEXTURE_32; // Prevent NPE
        LightTriangleRenderUnit lightTriangleRenderUnit = renderUnitsInstance.select(LightTriangleRenderUnit.class).get();
        lightTriangleRenderUnit.init(new Color(1.0, 1.0, 1.0));
        return lightTriangleRenderUnit;
    }

    private WireTriangleRenderUnit createWireRenderer(ImageDescriptor imageDescriptor) {
        currentImageDescriptor = imageDescriptor;
        WireTriangleRenderUnit wireTriangleRenderUnit = renderUnitsInstance.select(WireTriangleRenderUnit.class).get();
        wireTriangleRenderUnit.init(imageDescriptor);
        return wireTriangleRenderUnit;
    }

    private AbstractTriangleRenderUnit createMultiTextureRenderer(ImageDescriptor topImageDescriptor, ImageDescriptor blendImageDescriptor, ImageDescriptor bottomImageDescriptor) {
        currentImageDescriptor = topImageDescriptor;
        MultiTextureTriangleRenderUnit multiTextureTriangleRenderUnit = renderUnitsInstance.select(MultiTextureTriangleRenderUnit.class).get();
        multiTextureTriangleRenderUnit.init(topImageDescriptor, blendImageDescriptor, bottomImageDescriptor);
        return multiTextureTriangleRenderUnit;
    }

}
