package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.VertexListService;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Sphere;
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
    private Logger logger = Logger.getLogger(TerrainObjectService.class.getName());
    @Inject
    private Caller<VertexListService> serviceCaller;
    @Inject
    private RenderService renderService;
    private VertexList vertexList;
    private List<Matrix4> positions = new ArrayList<>();
    private ImageDescriptor imageDescriptor = ImageDescriptor.BUSH_1;

    public TerrainObjectService() {
//        for (int x = 0; x < 30; x++) {
//            for (int y = 0; y < 30; y++) {
//                double angleZ = Math.random() * MathHelper.ONE_RADIANT;
//                double translateX = Math.random() * 130;
//                double translateY = Math.random() * 130;
//                Matrix4 matrix4 = Matrix4.createTranslation(x * 130 + translateX, y * 130 + translateY, 0);
//                double scale = Math.random() * 2.0 + 4.0;
//                matrix4 = matrix4.multiply(Matrix4.createScale(scale, scale, scale));
//                // matrix4 = base.multiply(matrix4);
//                matrix4 = matrix4.multiply(Matrix4.createZRotation(angleZ));
//                positions.add(matrix4);
//            }
//        }

        vertexList = new Sphere(15, 10, 10).provideVertexList(ImageDescriptor.BUSH_1);
        // vertexList = new Plane(100).provideVertexListPlain(AbstractRenderer.CHESS_TEXTURE_08);

        positions.add(Matrix4.createTranslation(450, 400, 0));
        //    positions.add(base.multiply(Matrix4.createTranslation(600, 200, 5)));

    }

    public VertexList getVertexList() {
        return vertexList;
    }

    public List<Matrix4> getPositions() {
        return positions;
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @AfterInitialization
    public void afterInitialization() {
        serviceCaller.call(new RemoteCallback<VertexList>() {
            @Override
            public void callback(final VertexList vertexList) {
                // TerrainObjectService.this.vertexList = vertexList;
                // renderService.fillBuffers();
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
