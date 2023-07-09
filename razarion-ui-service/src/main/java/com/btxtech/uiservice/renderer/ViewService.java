package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.nativejs.NativeMatrix;

import javax.enterprise.context.ApplicationScoped;

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

    public void addViewFieldListeners(ViewFieldListener viewFieldListener) {
    }

    public void removeViewFieldListeners(ViewFieldListener viewFieldListener) {
    }

    public ViewField getCurrentViewField() {
        return null;
    }

    public Rectangle2D getCurrentAabb() {
        return null;
    }

    public Rectangle2D getCurrentInnerAabb() {
        return null;
    }

    public NativeMatrix getViewMatrix() {
        return null;
    }

    public NativeMatrix getViewNormMatrix() {
        return null;
    }

    public NativeMatrix getPerspectiveMatrix() {
        return null;
    }

    public NativeMatrix getViewShadowMatrix() {
        return null;
    }

    public NativeMatrix getPerspectiveShadowMatrix() {
        return null;
    }

    public NativeMatrix getShadowLookupMatrix() {
        return null;
    }
}
