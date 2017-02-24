package com.btxtech.client.dialog.framework;

import com.btxtech.client.dialog.boxcontent.BoxContentDialog;
import com.btxtech.client.cockpit.level.LevelUpDialog;
import com.btxtech.client.cockpit.quest.QuestPassedDialog;
import com.btxtech.client.dialog.common.MessageDialog;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Singleton
public class ClientModalDialogManagerImpl extends ModalDialogManager {
    public enum Type {
        PROMPTLY,
        STACK_ABLE,
        QUEUE_ABLE,
        UNIMPORTANT
    }

    @Inject
    private Instance<ModalDialogPanel<Object>> containerInstance;
    @Inject
    private AudioService audioService;
    private ModalDialogPanel activeDialog;
    private List<DialogParameters> dialogQueue = new ArrayList<>();
    private List<ModalDialogPanel> stackedDialogs = new ArrayList<>();

    @Override
    protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener) {
        show("Quest bestanden", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, QuestPassedDialog.class, questDescriptionConfig, (button, value) -> closeListener.run(), null, audioService.getAudioConfig().getOnQuestPassed(), DialogButton.Button.CLOSE);
    }

    @Override
    protected void showLevelUp(UserContext userContext, Runnable closeListener) {
        show("Level Up", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, LevelUpDialog.class, null, (button, value) -> closeListener.run(), null, audioService.getAudioConfig().getOnLevelUp(), DialogButton.Button.CLOSE);
    }

    @Override
    public void showBoxPicked(BoxContent boxContent) {
        show("Box gesammelt", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BoxContentDialog.class, boxContent, null, null, audioService.getAudioConfig().getOnBoxPicked(), DialogButton.Button.CLOSE);
    }

    @Override
    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        show(I18nHelper.getConstants().useItem(), ClientModalDialogManagerImpl.Type.STACK_ABLE, MessageDialog.class, I18nHelper.getConstants().useItemLimit(I18nHelper.getLocalizedString(baseItemType.getI18Name())), null, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void showUseInventoryHouseSpaceExceeded() {
        show(I18nHelper.getConstants().useItem(), ClientModalDialogManagerImpl.Type.STACK_ABLE, MessageDialog.class, I18nHelper.getConstants().useItemHouseSpace(), null, null, DialogButton.Button.CLOSE);
    }

    @Override
    protected void showBaseLost(Runnable closeListener) {
        show(I18nHelper.getConstants().baseLostTitle(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, MessageDialog.class, I18nHelper.getConstants().baseLost(), (button, value) -> closeListener.run(), null, audioService.getAudioConfig().getOnBaseLost(), DialogButton.Button.CLOSE);
    }

    public void showMessageDialog(String title, String message) {
        show(title, Type.STACK_ABLE, MessageDialog.class, message, null, null, DialogButton.Button.CLOSE);
    }

    public <T> void show(String title, Type type, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, Runnable shownCallback, DialogButton.Button... dialogButtons) {
        show(title, type, contentClass, t, listener, shownCallback, audioService.getAudioConfig().getDialogOpened(), dialogButtons);
    }

    public <T> void show(String title, Type type, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, Runnable shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
        if (activeDialog == null) {
            showDialog(title, contentClass, t, listener, shownCallback, audioId, dialogButtons);
        } else {
            switch (type) {
                case PROMPTLY:
                    close(activeDialog);
                    showDialog(title, contentClass, t, listener, shownCallback, audioId, dialogButtons);
                    break;
                case QUEUE_ABLE:
                    dialogQueue.add(new DialogParameters(title, contentClass, t, listener, shownCallback, audioId, dialogButtons));
                    break;
                case STACK_ABLE:
                    showStackedDialog(title, contentClass, t, listener, shownCallback, audioId, dialogButtons);
                    break;
                case UNIMPORTANT:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown dialog type: " + type);
            }
        }
    }

    private void showDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener listener, Runnable shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
        ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
        modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, listener, dialogButtons);
        this.activeDialog = modalDialogPanel;
        showDialog(activeDialog, shownCallback, audioId);
    }

    private void showStackedDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener listener, Runnable shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
        ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
        modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, listener, dialogButtons);
        stackedDialogs.add(modalDialogPanel);
        showDialog(modalDialogPanel, shownCallback, audioId);
    }

    private void showDialog(ModalDialogPanel modalDialogPanel, Runnable shownCallback, Integer audioId) {
        audioService.onDialogOpened(audioId);
        RootPanel.get().add(modalDialogPanel);
        if (shownCallback != null) {
            shownCallback.run();
        }
    }

    public void close(ModalDialogPanel modalDialogPanel) {
        audioService.onDialogClosed();
        modalDialogPanel.onClose();
        RootPanel.get().remove(modalDialogPanel);
        if (modalDialogPanel == activeDialog) {
            activeDialog = null;
            if (!dialogQueue.isEmpty()) {
                dialogQueue.remove(0).showDialog();
            }
        } else if (stackedDialogs.contains(modalDialogPanel)) {
            stackedDialogs.remove(modalDialogPanel);
        }
    }

    private class DialogParameters {
        private String title;
        private Class<? extends ModalDialogContent> contentClass;
        private Object object;
        private DialogButton.Listener<?> listener;
        private Runnable shownCallback;
        private Integer audioId;
        private DialogButton.Button[] dialogButtons;

        DialogParameters(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener<?> listener, Runnable shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
            this.title = title;
            this.contentClass = contentClass;
            this.object = object;
            this.listener = listener;
            this.shownCallback = shownCallback;
            this.audioId = audioId;
            this.dialogButtons = dialogButtons;
        }

        void showDialog() {
            ClientModalDialogManagerImpl.this.showDialog(title, contentClass, object, listener, shownCallback, audioId, dialogButtons);
        }
    }
}
