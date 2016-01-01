package com.btxtech.client.units;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.VertexListService;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
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
 * 28.12.2015.
 */
@Singleton
public class UnitService {
    private Logger logger = Logger.getLogger(UnitService.class.getName());
    private VertexList vertexList;
    private ImageDescriptor imageDescriptor = ImageDescriptor.UNIT_TEXTURE_O1;
    private double specularIntensity = 0.1;
    private double specularHardness = 10;
    @Inject
    private Caller<VertexListService> vertexListService;
    @Inject
    private RenderService renderService;


    @AfterInitialization
    public void afterInitialization() {
        vertexListService.call(new RemoteCallback<List<VertexList>>() {
            @Override
            public void callback(List<VertexList> response) {
                vertexList = new VertexList();
                for (VertexList vertexList : response) {
                    logger.severe("vertexList: " + vertexList.getName() + ":" + vertexList.getVertices().size());
                    UnitService.this.vertexList.append(vertexList);
                }
                renderService.fillBuffers();
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, message != null ? message.toString() : "Message is null", throwable);
                return false;
            }
        }).getUnit();
    }

    public VertexList getVertexList() {
        return vertexList;
    }

    public Matrix4 getModelMatrix() {
         return Matrix4.createTranslation(355, 300, 0);
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }
}
