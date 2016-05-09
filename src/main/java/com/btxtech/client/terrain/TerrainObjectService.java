package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.shared.VertexListService;
import com.btxtech.client.renderer.DepthSorter;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.09.2015.
 */
@Singleton
public class TerrainObjectService {
    // private static final int EDGE_COUNT = 10;
    private static final int EDGE_COUNT = 0;
    private static final String TRUNK_MESH = "Trunk Mesh";
    private static final String TWIG_MESH = "Twig Mesh";
    private static final String SHADOW_MESH = "Shadow Mesh";
    private Logger logger = Logger.getLogger(TerrainObjectService.class.getName());
    @Inject
    private Caller<VertexListService> serviceCaller;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Camera camera;
    private VertexList opaqueVertexList;
    private VertexList totalOpaqueVertexList = new VertexList();
    private VertexList transparentVertexList;
    private VertexList totalTransparentVertexList = new VertexList();
    private VertexList shadowTransparentVertexList;
    private VertexList totalShadowTransparentVertexList = new VertexList();
    private ImageDescriptor opaqueDescriptor = ImageDescriptor.SAND_2;
    private ImageDescriptor transparentDescriptor = ImageDescriptor.BRANCH_01;

    public ImageDescriptor getOpaqueDescriptor() {
        return opaqueDescriptor;
    }

    public ImageDescriptor getTransparentDescriptor() {
        return transparentDescriptor;
    }

    public VertexList getTotalTransparentVertexList() {
        return totalTransparentVertexList;
    }

    public VertexList getTotalOpaqueVertexList() {
        return totalOpaqueVertexList;
    }

    public VertexList getTotalShadowTransparentVertexList() {
        return totalShadowTransparentVertexList;
    }

    @AfterInitialization
    public void afterInitialization() {
//        serviceCaller.call(new RemoteCallback<List<VertexList>>() {
//            @Override
//            public void callback(final List<VertexList> vertexLists) {
//                try {
//                    for (VertexList vertexList : vertexLists) {
//                        switch (vertexList.getName()) {
//                            case TRUNK_MESH:
//                                opaqueVertexList = vertexList;
//                                break;
//                            case TWIG_MESH:
//                                transparentVertexList = vertexList;
//                                break;
//                            case SHADOW_MESH:
//                                shadowTransparentVertexList = vertexList;
//                                break;
//
//                        }
//                        logger.severe("TerrainObjectService loaded: " + vertexList.getName() + " size: " + vertexList.getVertices().size());
//                    }
//                    setupTriangles();
//                    renderService.fillBuffers();
//                } catch(Throwable throwable) {
//                    logger.log(Level.SEVERE, throwable.getMessage(), throwable);
//                }
//            }
//        }, new ErrorCallback() {
//            @Override
//            public boolean error(Object message, Throwable throwable) {
//                logger.log(Level.SEVERE, "message: " + message, throwable);
//                return false;
//            }
//
//        }).getVertexList();
    }

    private void setupTriangles() {
        for (int x = 0; x < EDGE_COUNT; x++) {
            for (int y = 0; y < EDGE_COUNT; y++) {
                double angleZ = Math.random() * MathHelper.ONE_RADIANT;
                double translateX = Math.random() * TerrainSurface.MESH_NODES;
                double translateY = Math.random() * TerrainSurface.MESH_NODES;
//    TODO            if(!terrainSurface.isFree(x * TerrainSurface.MESH_NODES + translateX, y * TerrainSurface.MESH_NODES + translateY)) {
//    TODO                continue;
//     TODO           }
                Matrix4 matrix4 = Matrix4.createTranslation(x * TerrainSurface.MESH_NODES + translateX, y * TerrainSurface.MESH_NODES + translateY, 0);
                double scale = Math.random() * 0.5 + 0.4;
                matrix4 = matrix4.multiply(Matrix4.createScale(scale, scale, scale));
                // matrix4 = base.multiply(matrix4);
                matrix4 = matrix4.multiply(Matrix4.createZRotation(angleZ));
                totalTransparentVertexList.append(matrix4, transparentVertexList);
                totalOpaqueVertexList.append(matrix4, opaqueVertexList);
                totalShadowTransparentVertexList.append(matrix4, shadowTransparentVertexList);
            }
        }
        totalTransparentVertexList = DepthSorter.depthSort(totalTransparentVertexList, camera.createMatrix());
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
