package com.btxtech.client.editor.widgets.audio;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.utils.HumanReadableIntegerSizeConverter;
import com.btxtech.shared.dto.AudioItemConfig;
import com.btxtech.shared.rest.AudioProvider;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import elemental.client.Browser;
import elemental.html.AudioElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("AudioWidget.html#audio-widget")
public class AudioWidget extends Composite {
    private Logger logger = Logger.getLogger(AudioWidget.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<AudioProvider> audioService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button chooseButton;
    private Integer audioId;
    private Consumer<Integer> audioItemWidgetListener;

    public void init(Integer audioId, Consumer<Integer> audioItemWidgetListener) {
        this.audioId = audioId;
        this.audioItemWidgetListener = audioItemWidgetListener;
        if (audioId != null) {
            ((AudioElement) audio).setSrc(CommonUrl.getAudioServiceUrl(audioId));
            audioService.call(new RemoteCallback<AudioItemConfig>() {
                @Override
                public void callback(AudioItemConfig audioItemConfig) {
                    dataBinder.setModel(audioItemConfig);
                }
            }, (message, throwable) -> {
                logger.log(Level.SEVERE, "getAudioItemConfig() failed: " + message, throwable);
                return false;
            }).getAudioItemConfig(audioId);
        }
    }

    public Integer getAudioId() {
        if (dataBinder.getModel() != null) {
            return dataBinder.getModel().getId();
        } else {
            return null;
        }
    }

    @EventHandler("chooseButton")
    private void chooseButtonClicked(ClickEvent event) {
        modalDialogManager.show("Audio Gallery", ClientModalDialogManagerImpl.Type.STACK_ABLE, AudioSelectorDialog.class, audioId, (button, id1) -> {
            if (button == DialogButton.Button.APPLY) {
                Integer old = audioId;
                audioId = id1;
                audioItemWidgetListener.accept(audioId);
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

}
