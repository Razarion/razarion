package com.btxtech.client.cockpit;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 27.02.2017.
 */
@Templated("EmptyCover.html#emptyCover")
public class EmptyCover implements IsElement {
    @Inject
    @DataField
    private HTMLDivElement emptyCover;

    @Override
    public HTMLElement getElement() {
        return emptyCover;
    }

    public void startFadeout() {
        emptyCover.style.zIndex = ZIndexConstants.EMPTY_COVER;
        // Newly added element is batching reflows by browsers
        // http://stackoverflow.com/questions/12088819/css-transitions-on-new-elements
        DomGlobal.requestAnimationFrame(timestamp -> {
            emptyCover.classList.add("empty-cover-opacity");
            return null;
        }, emptyCover);
    }
}
