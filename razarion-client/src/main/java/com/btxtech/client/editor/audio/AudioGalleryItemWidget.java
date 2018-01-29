package com.btxtech.client.editor.audio;

import com.btxtech.client.editor.widgets.FileButton;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.HumanReadableIntegerSizeConverter;
import com.btxtech.shared.CommonUrl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.TakesValue;
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
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.12.2016.
 */
@Templated("AudioDialog.html#audioGalleryItemWidget")
public class AudioGalleryItemWidget implements TakesValue<AudioGalleryItem>, IsElement {
    @Inject
    @DataField
    private Table audioGalleryItemWidget;
    @Inject
    @AutoBound
    private DataBinder<AudioGalleryItem> dataBinder;
    @DataField
    private Element audio = (Element) Browser.getDocument().createAudioElement();
    @Inject
    @Bound(converter = HumanReadableIntegerSizeConverter.class)
    @DataField
    private Label size;
    @Bound
    @Inject
    @DataField
    private Label id;
    @Bound
    @Inject
    @DataField
    private Label type;
    @Bound
    @Inject
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private FileButton uploadButton;

    @Override
    public void setValue(AudioGalleryItem audioGalleryItem) {
        dataBinder.setModel(audioGalleryItem);
        ((AudioElement) audio).setSrc(CommonUrl.getAudioServiceUrl(audioGalleryItem.getId()));
        uploadButton.init("Upload", fileList -> ControlUtils.readFirstAsDataURL(fileList, (dataUrl, file) -> {
            ((AudioElement) audio).setSrc(dataUrl);
            dataBinder.getModel().setDataUrl(dataUrl);
        }));
    }

    @Override
    public AudioGalleryItem getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return audioGalleryItemWidget;
    }
}
