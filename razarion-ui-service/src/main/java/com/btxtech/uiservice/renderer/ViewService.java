package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.nativejs.NativeMatrix;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 25.03.2017.
 */
@ApplicationScoped
public class ViewService {
    public interface TransformationListener {
        void onTransformationChanged(NativeMatrix viewMatrix, NativeMatrix perspectiveMatrix);
    }

    public interface TransformationNormListener {
        void onTransformationChanged(NativeMatrix viewMatrix, NativeMatrix viewNormMatrix, NativeMatrix perspectiveMatrix);
    }

    public interface ShadowTransformationListener {
        void onTransformationChanged(NativeMatrix viewShadowMatrix, NativeMatrix perspectiveShadowMatrix);
    }

    public interface ShadowLookupTransformationListener {
        void onShadowLookupTransformationChanged(NativeMatrix shadowLookupMatrix);
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
    private NativeMatrix viewMatrix;
    private NativeMatrix viewNormMatrix;
    private NativeMatrix perspectiveMatrix;
    private NativeMatrix viewShadowMatrix;
    private NativeMatrix perspectiveShadowMatrix;
    private NativeMatrix shadowLookupMatrix;
    private Collection<TransformationListener> transformationListeners = new ArrayList<>();
    private Collection<TransformationNormListener> transformationNormListeners = new ArrayList<>();
    private Collection<ShadowTransformationListener> shadowTransformationListeners = new ArrayList<>();
    private Collection<ShadowLookupTransformationListener> shadowLookupTransformationListeners = new ArrayList<>();
    private ViewField currentViewField;
    private Rectangle2D currentAabb;

    public Runnable addAndCallTransformationListener(TransformationListener listener) {
        transformationListeners.add(listener);
        if (viewMatrix == null || perspectiveMatrix == null) {
            updateTransformationMatrices();
        }
        listener.onTransformationChanged(viewMatrix, perspectiveMatrix);
        return () -> transformationListeners.remove(listener);
    }

    public Runnable addAndCallTransformationNormListener(TransformationNormListener listener) {
        transformationNormListeners.add(listener);
        if (viewMatrix == null || viewNormMatrix == null || perspectiveMatrix == null) {
            updateTransformationMatrices();
        }
        listener.onTransformationChanged(viewMatrix, viewNormMatrix, perspectiveMatrix);
        return () -> transformationNormListeners.remove(listener);
    }

    public Runnable addAndCallShadowTransformationListener(ShadowTransformationListener listener) {
        shadowTransformationListeners.add(listener);
        if (viewShadowMatrix == null || perspectiveShadowMatrix == null) {
            updateTransformationMatrices();
        }
        listener.onTransformationChanged(viewShadowMatrix, perspectiveShadowMatrix);
        return () -> shadowTransformationListeners.remove(listener);
    }

    public Runnable addAndCallShadowLookupTransformationListener(ShadowLookupTransformationListener listener) {
        shadowLookupTransformationListeners.add(listener);
        if (shadowLookupMatrix == null) {
            updateTransformationMatrices();
        }
        listener.onShadowLookupTransformationChanged(shadowLookupMatrix);
        return () -> shadowLookupTransformationListeners.remove(listener);
    }

    public void onViewChanged() {
        updateTransformationMatrices();
        transformationListeners.forEach(listeners -> listeners.onTransformationChanged(viewMatrix, perspectiveMatrix));
        transformationNormListeners.forEach(listeners -> listeners.onTransformationChanged(viewMatrix, viewNormMatrix, perspectiveMatrix));
        shadowTransformationListeners.forEach(listeners -> listeners.onTransformationChanged(viewShadowMatrix, perspectiveShadowMatrix));
        shadowLookupTransformationListeners.forEach(listeners -> listeners.onShadowLookupTransformationChanged(shadowLookupMatrix));
        terrainUiService.onViewChanged(currentViewField, currentAabb);
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
    }

    public ViewField getCurrentViewField() {
        return currentViewField;
    }

    public Rectangle2D getCurrentAabb() {
        return currentAabb;
    }
}