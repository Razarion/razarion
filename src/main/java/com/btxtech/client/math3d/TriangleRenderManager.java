package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Singleton
public class TriangleRenderManager {
    public enum Mode {
        NORMAL,
        MULTI_TEX,
        LIGHT,
        WIRE_08,
        WIRE_32,
        DEV_TEX_08,
        DEV_TEX_32
    }

    private List<TriangleRenderHolder> renderers = new ArrayList<>();
    private Logger logger = Logger.getLogger(TriangleRenderManager.class.getName());
    private Mode mode = Mode.NORMAL;
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private ModelTransformation modelTransformation;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Lighting lighting;
    @Inject
    private Instance<TriangleRenderHolder> holderInstance;

    public ModelTransformation getModelTransformation() {
        return modelTransformation;
    }

    public ViewTransformation getViewTransformation() {
        return viewTransformation;
    }

    public ProjectionTransformation getProjectionTransformation() {
        return projectionTransformation;
    }

    public Lighting getLighting() {
        return lighting;
    }

    public void createTriangleRenderUnit(VertexListProvider vertexListProvider, ImageDescriptor imageDescriptor) {
        TriangleRenderHolder triangleRenderHolder = holderInstance.get();
        triangleRenderHolder.setVertexListProvider(vertexListProvider);
        triangleRenderHolder.setImageDescriptor(imageDescriptor);
        triangleRenderHolder.createTriangleRenderUnit(mode);
        renderers.add(triangleRenderHolder);
    }

    public void draw() {
        for (TriangleRenderHolder renderDescriptor : renderers) {
            try {
                renderDescriptor.draw();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "draw failed", t);
            }
        }
    }

    public void fillBuffers() {
        for (TriangleRenderHolder renderDescriptor : renderers) {
            try {
                renderDescriptor.fillBuffers();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "fillBuffers failed", t);
            }
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        for (TriangleRenderHolder renderDescriptor : renderers) {
            try {
                renderDescriptor.createTriangleRenderUnit(mode);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "fillBuffers failed", t);
            }
        }
    }

    public Mode getMode() {
        return mode;
    }
}
