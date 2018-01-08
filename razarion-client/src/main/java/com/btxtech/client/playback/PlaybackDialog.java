package com.btxtech.client.playback;

import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Templated("PlaybackDialog.html#playbackDialogDiv")
public class PlaybackDialog implements IsElement {
    @Inject
    @DataField
    private Div playbackDialogDiv;
    @Inject
    @DataField
    private Span titleSpan;

    public void init(String title, int left, int top, int width, int height, int zIndex) {
        playbackDialogDiv.getStyle().setProperty("left", left + "px");
        playbackDialogDiv.getStyle().setProperty("top", top + "px");
        playbackDialogDiv.getStyle().setProperty("width", width + "px");
        playbackDialogDiv.getStyle().setProperty("height", height + "px");
        playbackDialogDiv.getStyle().setProperty("z-index", Integer.toString(zIndex));
        titleSpan.setTextContent(title);
    }

    @Override
    public HTMLElement getElement() {
        return playbackDialogDiv;
    }
}
