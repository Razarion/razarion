package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 25.03.2017.
 */
@Deprecated
@ApplicationScoped
public class ViewService {
    public interface ViewFieldListener {
        void onViewChanged(ViewField viewField, Rectangle2D absAabbRect);
    }

    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private NativeMatrix viewMatrix;
    private NativeMatrix viewNormMatrix;
    private NativeMatrix perspectiveMatrix;
    private NativeMatrix viewShadowMatrix;
    private NativeMatrix perspectiveShadowMatrix;
    private NativeMatrix shadowLookupMatrix;
    private Collection<ViewFieldListener> viewFieldListeners = new ArrayList<>();
    private ViewField currentViewField;
    private Rectangle2D currentAabb;
    private Rectangle2D currentInnerAabb;


    public void addViewFieldListeners(ViewFieldListener viewFieldListener) {
        viewFieldListeners.add(viewFieldListener);
    }

    public void removeViewFieldListeners(ViewFieldListener viewFieldListener) {
        viewFieldListeners.remove(viewFieldListener);
    }

    public void onViewChanged() {
        if(!terrainUiService.isLoaded()) {
            return;
        }
        updateTransformationMatrices();
        terrainUiService.onViewChanged(currentViewField);
        // Prevent concurrent exception with scene, tip etc
        new ArrayList<>(viewFieldListeners).forEach(viewFieldListener -> {
            try {
                viewFieldListener.onViewChanged(currentViewField, currentAabb);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });
    }

    private void updateTransformationMatrices() {
        if (camera.getMatrix() == null || camera.getNormMatrix() == null || projectionTransformation.getMatrix() == null
                || shadowUiService.getDepthViewTransformation() == null || shadowUiService.getDepthProjectionTransformation() == null) {
            camera.setupMatrices();
        }
        viewMatrix = nativeMatrixFactory.createFromColumnMajorArray(camera.getMatrix().toWebGlArray());
        viewNormMatrix = nativeMatrixFactory.createFromColumnMajorArray(camera.getNormMatrix().toWebGlArray());
        perspectiveMatrix = nativeMatrixFactory.createFromColumnMajorArray(projectionTransformation.getMatrix().toWebGlArray());
        viewShadowMatrix = nativeMatrixFactory.createFromColumnMajorArray(shadowUiService.getDepthViewTransformation().toWebGlArray());
        perspectiveShadowMatrix = nativeMatrixFactory.createFromColumnMajorArray(shadowUiService.getDepthProjectionTransformation().toWebGlArray());
        shadowLookupMatrix = nativeMatrixFactory.createFromColumnMajorArray(shadowUiService.getShadowLookupTransformation().toWebGlArray());
        currentViewField = projectionTransformation.calculateViewField(0);
        currentAabb = currentViewField.calculateAabbRectangle();
        currentInnerAabb = currentViewField.calculateInnerAabbRectangle();
    }

    public ViewField getCurrentViewField() {
        return currentViewField;
    }

    public Rectangle2D getCurrentAabb() {
        return currentAabb;
    }

    public Rectangle2D getCurrentInnerAabb() {
        return currentInnerAabb;
    }

    public NativeMatrix getViewMatrix() {
        return viewMatrix;
    }

    public NativeMatrix getViewNormMatrix() {
        return viewNormMatrix;
    }

    public NativeMatrix getPerspectiveMatrix() {
        return perspectiveMatrix;
    }

    public NativeMatrix getViewShadowMatrix() {
        return viewShadowMatrix;
    }

    public NativeMatrix getPerspectiveShadowMatrix() {
        return perspectiveShadowMatrix;
    }

    public NativeMatrix getShadowLookupMatrix() {
        return shadowLookupMatrix;
    }
}
