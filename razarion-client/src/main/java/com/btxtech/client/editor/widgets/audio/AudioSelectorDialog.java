package com.btxtech.client.editor.widgets.audio;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 19.06.2016.
 */
@Templated("AudioSelectorDialog.html#audio-selector-dialog")
public class AudioSelectorDialog extends Composite implements ModalDialogContent<Integer> {
    private Logger logger = Logger.getLogger(AudioSelectorDialog.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<AudioProvider> audioService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<AudioItemConfig, AudioSelectorItemWidget> audioGallery;
    private Integer selectedAudioId;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedAudioId) {
        this.selectedAudioId = selectedAudioId;
        DOMUtil.removeAllElementChildren(audioGallery.getElement()); // Remove placeholder table row from template.
        audioService.call(new RemoteCallback<List<AudioItemConfig>>() {
            @Override
            public void callback(List<AudioItemConfig> audioItemConfigs) {
                onLoaded(audioItemConfigs);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getAudioItemConfigs failed: " + message, throwable);
            return false;
        }).getAudioItemConfigs();

        audioGallery.addComponentCreationHandler(imageGalleryItemWidget -> imageGalleryItemWidget.setAudioSelectorDialog(this));
        audioGallery.setSelector(imageGalleryItemWidget -> imageGalleryItemWidget.setSelected(true));
        audioGallery.setDeselector(imageGalleryItemWidget -> imageGalleryItemWidget.setSelected(false));
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @Override
    public void onClose() {
        // Ignore
    }

    private void onLoaded(List<AudioItemConfig> audioItemConfigs) {
        audioGallery.setValue(audioItemConfigs);
        if (selectedAudioId != null) {
            audioItemConfigs.stream().filter(audioItemConfig -> audioItemConfig.getId() == selectedAudioId).forEach(audioItemConfig -> audioGallery.selectModel(audioItemConfig));
        }
    }

    public void selectionChanged(AudioItemConfig audioItemConfig) {
        audioGallery.deselectAll();
        audioGallery.selectModel(audioItemConfig);
        selectedAudioId = audioItemConfig.getId();
        audioGallery.getComponent(audioItemConfig).ifPresent(arg -> arg.setSelected(true));
        modalDialogPanel.setApplyValue(selectedAudioId);
    }
}
