package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.VertexListService;
import com.btxtech.client.renderer.engine.RenderService;
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
    private ImageDescriptor imageDescriptor = Terrain.BUSH_1;

    public TerrainObjectService() {
        Matrix4 base = Matrix4.createScale(0.1, 0.1, 0.1).multiply(Matrix4.createTranslation(200, 200, -90));

        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                double angleZ = Math.random() * MathHelper.ONE_RADIANT;
                double scale = Math.random() * 0.5 + 0.5;
                double translateX = Math.random() * 50;
                double translateY = Math.random() * 50;
                Matrix4 matrix4 = Matrix4.createTranslation(x * 130, y * 130, 0);
                matrix4 = matrix4.multiply(Matrix4.createZRotation(angleZ));
                matrix4 = matrix4.multiply(Matrix4.createScale(scale, scale, scale));
                matrix4 = matrix4.multiply(Matrix4.createTranslation(translateX, translateY, 0));
                matrix4 = base.multiply(matrix4);
                positions.add(matrix4);
            }
        }
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
                TerrainObjectService.this.vertexList = vertexList;
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
