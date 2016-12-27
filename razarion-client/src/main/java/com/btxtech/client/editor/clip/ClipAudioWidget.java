package com.btxtech.client.editor.clip;

import com.btxtech.client.editor.widgets.audio.AudioWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 27.12.2016.
 */
@Templated("ClipPropertyPanel.html#audioTableRow")
public class ClipAudioWidget implements TakesValue<Integer>, IsElement {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    // @Named("td")
    private TableRow audioTableRow;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private AudioWidget audioWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button deleteAudioButton;
    private ClipPropertyPanel clipPropertyPanel;

    @Override
    public void setValue(Integer audioId) {
        audioWidget.init(audioId, (oldId, newId) -> clipPropertyPanel.changeAudioId(oldId, newId));
    }

    @Override
    public Integer getValue() {
        return audioWidget.getAudioId();
    }

    @Override
    public HTMLElement getElement() {
        return audioTableRow;
    }

    void setClipPropertyPanel(ClipPropertyPanel clipPropertyPanel) {
        this.clipPropertyPanel = clipPropertyPanel;
    }

    @EventHandler("deleteAudioButton")
    private void deleteAudioButtonClick(ClickEvent event) {
        clipPropertyPanel.deleteAudio(getValue());
    }
}
