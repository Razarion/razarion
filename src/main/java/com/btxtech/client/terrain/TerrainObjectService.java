package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.VertexListService;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.09.2015.
 */
@Singleton
public class TerrainObjectService {
    private static final int EDGE_COUNT = 10;
    private static final String TRUNK_MESH = "Trunk Mesh";
    private static final String TWIG_MESH = "Twig Mesh";
    private Logger logger = Logger.getLogger(TerrainObjectService.class.getName());
    @Inject
    private Caller<VertexListService> serviceCaller;
    @Inject
    private RenderService renderService;
    private VertexList opaqueVertexList;
    private VertexList transparentVertexList;
    private List<Matrix4> positions = new ArrayList<>();
    private ImageDescriptor opaqueDescriptor = ImageDescriptor.SAND_2;
    private ImageDescriptor transparentDescriptor = ImageDescriptor.BRANCH_01;

    public TerrainObjectService() {
//        int edge = TerrainSurface.MESH_EDGE_SIZE / EDGE_COUNT;
//        for (int x = 0; x < EDGE_COUNT; x++) {
//            for (int y = 0; y < EDGE_COUNT; y++) {
//                double angleZ = Math.random() * MathHelper.ONE_RADIANT;
//                double translateX = Math.random() * edge;
//                double translateY = Math.random() * edge;
//                Matrix4 matrix4 = Matrix4.createTranslation(x * edge + translateX, y * edge + translateY, 0);
//                double scale = Math.random() * 2.0 + 4.0;
//                matrix4 = matrix4.multiply(Matrix4.createScale(scale, scale, scale));
//                // matrix4 = base.multiply(matrix4);
//                matrix4 = matrix4.multiply(Matrix4.createZRotation(angleZ));
//                positions.add(matrix4);
//            }
//        }

        // opaqueVertexList = new Sphere(15, 10, 10).provideVertexList(ImageDescriptor.BUSH_1);
        // opaqueVertexList = new Plane(100).provideVertexListPlain(AbstractRenderer.CHESS_TEXTURE_08);

        positions.add(Matrix4.createTranslation(450, 300, 0));
        // positions.add(Matrix4.createIdentity());
        // positions.add(base.multiply(Matrix4.createTranslation(600, 200, 5)));

    }

    public VertexList getOpaqueVertexList() {
        return opaqueVertexList;
    }

    public VertexList getTransparentVertexList() {
        return transparentVertexList;
    }

    public List<Matrix4> getPositions() {
        return positions;
    }

    public ImageDescriptor getOpaqueDescriptor() {
        return opaqueDescriptor;
    }

    public ImageDescriptor getTransparentDescriptor() {
        return transparentDescriptor;
    }

    @AfterInitialization
    public void afterInitialization() {
        serviceCaller.call(new RemoteCallback<List<VertexList>>() {
            @Override
            public void callback(final List<VertexList> vertexLists) {
                for (VertexList vertexList : vertexLists) {
                    switch (vertexList.getName()) {
                        case TRUNK_MESH:
                            opaqueVertexList = vertexList;
                            break;
                        case TWIG_MESH:
                            transparentVertexList = vertexList;
                            break;

                    }
                    logger.severe("TerrainObjectService loaded: " + vertexList.getName() + " size: " + vertexList.getVertices().size());
                }
                renderService.fillBuffers();
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "message: " + message, throwable);
                return false;
            }

        }).getVertexList();
    }

    @UncaughtException
    private void onUncaughtException(Throwable caught) {
        try {
            throw caught;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "An unexpected error has occurred", t);
        }

    }

}
