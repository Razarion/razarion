package com.btxtech.client.editor.clip;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.audio.AudioSelectorDialog;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.client.guielements.VertexBox;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.clip.EffectService;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("ClipPropertyPanel.html#clip-property-panel")
public class ClipPropertyPanel extends AbstractPropertyPanel<ClipConfig> {
    // private Logger logger = Logger.getLogger(ClipPropertyPanel.class.getName());
    @Inject
    @AutoBound
    private DataBinder<ClipConfig> dataBinder;
    @Inject
    private ClipCrud clipCrud;
    @Inject
    private Shape3DUiService clipService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private EffectService effectService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DId;
    @Inject
    @DataField
    private Label duration;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<Integer, ClipAudioWidget> audioIds;
    @Inject
    @DataField
    private Button createAudioButton;
    @Inject
    @DataField
    private VertexBox testPosition;
    @Inject
    @DataField
    private Button testPlayClipButton;

    @Override
    public void init(ClipConfig clipConfig) {
        DOMUtil.removeAllElementChildren(audioIds.getElement()); // Remove placeholder table row from template.
        audioIds.addComponentCreationHandler(clipAudioWidget -> clipAudioWidget.setClipPropertyPanel(this));
        dataBinder.setModel(clipConfig);
        onChange(clipConfig);
        shape3DId.init(clipConfig.getShape3DId(), shape3DId -> {
            clipConfig.setShape3DId(shape3DId);
            clipConfig.setDurationMillis(clipService.getShape3D(shape3DId).calculateAnimationDuration());
            duration.setText(DisplayUtils.handleInteger(clipConfig.getDurationMillis()));
        });
        if (clipConfig.getShape3DId() != null) {
            duration.setText(DisplayUtils.handleInteger(clipConfig.getDurationMillis()));
        }
    }

    @Override
    public ClipConfig getConfigObject() {
        return dataBinder.getModel();
    }

    @EventHandler("createAudioButton")
    private void createAudioButtonClick(ClickEvent event) {
        modalDialogManager.show("Audio Gallery", ClientModalDialogManagerImpl.Type.STACK_ABLE, AudioSelectorDialog.class, null, (button, id1) -> {
            if (button == DialogButton.Button.APPLY && id1 != null) {
                dataBinder.getModel().getAudioIds().add(id1);
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);

    }

    void deleteAudio(Integer audioId) {
        dataBinder.getModel().getAudioIds().remove(audioId);
    }

    void changeAudioId(Integer oldId, Integer newId) {
        dataBinder.getModel().getAudioIds().set(dataBinder.getModel().getAudioIds().indexOf(oldId), newId);
    }

    @EventHandler("testPlayClipButton")
    private void testPlayClipButtonClick(ClickEvent event) {
        try {
            Vertex position = testPosition.getVertex();
            if (position != null) {
                effectService.playClip(position, dataBinder.getModel().getId(), System.currentTimeMillis());
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

}
