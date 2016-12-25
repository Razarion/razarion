package com.btxtech.client.editor.audio;

import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.HumanReadableIntegerSizeConverter;
import com.btxtech.shared.rest.RestUrl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import elemental.client.Browser;
import elemental.html.AudioElement;
import org.jboss.errai.common.client.api.IsElement;
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
 * 24.12.2016.
 */
@Templated("AudioDialog.html#audioGalleryItemWidget")
public class AudioGalleryItemWidget implements TakesValue<AudioGalleryItem>, IsElement {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Table audioGalleryItemWidget;
    @Inject
    @AutoBound
    private DataBinder<AudioGalleryItem> dataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @DataField
    private Element audio = (Element) Browser.getDocument().createAudioElement();
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
    private TextBox internalName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button uploadButton;

    @Override
    public void setValue(AudioGalleryItem audioGalleryItem) {
        dataBinder.setModel(audioGalleryItem);
        ((AudioElement) audio).setSrc(RestUrl.getAudioServiceUrl(audioGalleryItem.getId()));
    }

    @Override
    public AudioGalleryItem getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return audioGalleryItemWidget;
    }

    @EventHandler("uploadButton")
    private void uploadButtonClick(ClickEvent event) {
        ControlUtils.openSingleFileDataUrlUpload((dataUrl, file) -> {
            ((AudioElement) audio).setSrc(dataUrl);
            dataBinder.getModel().setDataUrl(dataUrl);
        });
    }
}
