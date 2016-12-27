package com.btxtech.client.editor.widgets.audio;

import com.btxtech.client.utils.HumanReadableIntegerSizeConverter;
import com.btxtech.shared.dto.AudioItemConfig;
import com.btxtech.shared.rest.RestUrl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import elemental.client.Browser;
import elemental.html.AudioElement;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Table;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("AudioSelectorDialog.html#audioSelectorItemWidget")
public class AudioSelectorItemWidget implements TakesValue<AudioItemConfig>, IsElement {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Table audioSelectorItemWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @DataField
    private Element audio = (Element) Browser.getDocument().createAudioElement();
    @Inject
    @AutoBound
    private DataBinder<AudioItemConfig> dataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound(converter = HumanReadableIntegerSizeConverter.class)
    @DataField
    private Label size;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound
    @Inject
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound
    @Inject
    @DataField
    private Label type;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound
    @Inject
    @DataField
    private Label internalName;
    private AudioSelectorDialog audioSelectorDialog;

    @Override
    public HTMLElement getElement() {
        return audioSelectorItemWidget;
    }

    @Override
    public void setValue(AudioItemConfig audioItemConfig) {
        ((AudioElement) audio).setSrc(RestUrl.getAudioServiceUrl(audioItemConfig.getId()));
        dataBinder.setModel(audioItemConfig);
        setSelected(false);
    }

    @Override
    public AudioItemConfig getValue() {
        return dataBinder.getModel();
    }

    @EventHandler("audioSelectorItemWidget")
    public void onClick(final ClickEvent event) {
        audioSelectorDialog.selectionChanged(dataBinder.getModel());
    }

    void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(audioSelectorItemWidget, "gallery-item-table-selected");
            DOMUtil.removeCSSClass(audioSelectorItemWidget, "gallery-item-table-not-selected");
        } else {
            DOMUtil.addCSSClass(audioSelectorItemWidget, "gallery-item-table-not-selected");
            DOMUtil.removeCSSClass(audioSelectorItemWidget, "gallery-item-table-selected");
        }
    }

    void setAudioSelectorDialog(AudioSelectorDialog audioSelectorDialog) {
        this.audioSelectorDialog = audioSelectorDialog;
    }
}
