package com.btxtech.client.editor.audio;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.shared.dto.AudioItemConfig;
import com.btxtech.shared.rest.AudioProvider;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 23.12.2016.
 */
@Templated("AudioDialog.html#audio-gallery-dialog")
public class AudioGalleryDialog extends Composite implements ModalDialogContent<Void> {
    private Logger logger = Logger.getLogger(AudioGalleryDialog.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<AudioProvider> audioService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<AudioGalleryItem, AudioGalleryItemWidget> audioGallery;

    @Override
    public void init(Void ignore) {
        DOMUtil.removeAllElementChildren(audioGallery.getElement()); // Remove placeholder table row from template.
        load();
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        modalDialogPanel.addNonClosableFooterButton("Reload", this::load);
        modalDialogPanel.addNonClosableFooterButton("New", () -> ControlUtils.openSingleFileDataUrlUpload((dataUrl, file) -> create(dataUrl)));
        modalDialogPanel.addNonClosableFooterButton("Save", this::save);
    }

    private void create(String dataUrl) {
        audioService.call(aVoid -> load(), (message, throwable) -> {
            logger.log(Level.SEVERE, "createAudio failed: " + message, throwable);
            return false;
        }).createAudio(dataUrl);
    }

    private void load() {
        audioService.call(new RemoteCallback<List<AudioItemConfig>>() {
            @Override
            public void callback(List<AudioItemConfig> audioItemConfigs) {
                List<AudioGalleryItem> audioGalleryItems = new ArrayList<>();
                for (AudioItemConfig audioItemConfig : audioItemConfigs) {
                    audioGalleryItems.add(new AudioGalleryItem().init(audioItemConfig));
                }
                audioGallery.setValue(audioGalleryItems);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getAllAudios failed: " + message, throwable);
            return false;
        }).getAllAudios();
    }

    @Override
    public void onClose() {
        // Ignore
    }

    private void save() {
        List<AudioItemConfig> changed = new ArrayList<>();
        for (AudioGalleryItem audioGalleryItem : audioGallery.getValue()) {
            if (audioGalleryItem.isDirty()) {
                changed.add(audioGalleryItem.createAudioConfig());
            }
        }
        if (changed.isEmpty()) {
            return;
        }
        audioService.call(aVoid -> load(), (message, throwable) -> {
            logger.log(Level.SEVERE, "createAudio failed: " + message, throwable);
            return false;
        }).save(changed);
    }
}
