package com.btxtech.client;

import com.btxtech.shared.Shape3DProvider;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.Shape3DUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.08.2016.
 */
@ApplicationScoped
public class ClientShape3DUiService extends Shape3DUiService {
    private Logger logger = Logger.getLogger(ClientShape3DUiService.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<Shape3DProvider> caller;

    public void create(String dataUrl, final Consumer<List<Shape3D>> callback) {
        caller.call(nothing -> reload(callback), (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).create(dataUrl);
    }

    public void reload(final Consumer<List<Shape3D>> callback) {
        caller.call(new RemoteCallback<List<Shape3D>>() {
            @Override
            public void callback(List<Shape3D> shape3Ds) {
                fillShape3Ds(shape3Ds);
                callback.accept(shape3Ds);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).getShape3Ds();
    }

    @Override
    protected void convertColladaText(String colladaText, final Consumer<Shape3D> callback) {
        caller.call(new RemoteCallback<Shape3D>() {
            @Override
            public void callback(Shape3D shape3D) {
                callback.accept(shape3D);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).colladaConvert(colladaText);
    }
}
