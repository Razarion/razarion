package com.btxtech.client;

import com.btxtech.client.math3d.TriangleRenderManager;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.client.terrain.SimpleTerrain;
import com.btxtech.client.terrain.Terrain;
import com.btxtech.client.terrain.Terrain2;
import com.btxtech.client.terrain.VertexList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@EntryPoint
public class Webgl /*implements EntryPoint*/ {
    public static Webgl instance;
    // @Inject
    private Logger logger = Logger.getLogger(Webgl.class.getName());
    @Inject
    private TriangleRenderManager triangleRenderManager;
    @Inject
    private Terrain2 terrain;
    @Inject
    private Caller<VertexListService> serviceCaller;

    public Webgl() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                if (logger != null) {
                    logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
                } else {
                    GWT.log("UncaughtExceptionHandler", e);
                }
            }
        });
    }

    @AfterInitialization
    public void afterInitialization() {
        serviceCaller.call(new RemoteCallback<VertexList>() {
            @Override
            public void callback(final VertexList vertexList) {
                logger.log(Level.SEVERE, "vertexList: " + vertexList);
                triangleRenderManager.createTriangleRenderUnit(new VertexListProvider() {
                    @Override
                    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
                        return vertexList;
                    }
                }, Terrain.BUSH_1);
                triangleRenderManager.fillBuffers();
            }
        }, new ErrorCallback() {

            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "message: " + message, throwable);
                return false;
            }

        }).getVertexList();
    }

    public void fillBuffers() {
//        terrain.setupTerrain();
//        triangleRenderManager.fillBuffers();
    }
//
////    @UncaughtException
////    private void onUncaughtException(Throwable caught) {
////        try {
////            throw caught;
////        } catch (Throwable t) {
////            GWT.log("An unexpected error has occurred", t);
////        }
////
////    }

}
