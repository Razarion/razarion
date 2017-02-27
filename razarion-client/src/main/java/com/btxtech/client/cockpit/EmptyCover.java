package com.btxtech.client.cockpit;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * 27.02.2017.
 */
@Templated("EmptyCover.html#emptyCover")
public class EmptyCover extends Composite {
    public void startFadeout() {
        getElement().getStyle().setZIndex(ZIndexConstants.EMPTY_COVER);
        // Newly added element is batching reflows by browsers
        // http://stackoverflow.com/questions/12088819/css-transitions-on-new-elements
        AnimationScheduler.get().requestAnimationFrame(timestamp -> getElement().addClassName("empty-cover-opacity"), getElement());
    }
}
