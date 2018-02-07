package com.btxtech.client.dialog.framework;

import com.btxtech.client.cockpit.quest.QuestPassedDialog;
import com.btxtech.client.dialog.boxcontent.BoxContentDialog;
import com.btxtech.client.dialog.common.MessageDialog;
import com.btxtech.client.dialog.common.MessageImage;
import com.btxtech.client.dialog.common.MessageImageDialog;
import com.btxtech.client.dialog.common.RegisterDialog;
import com.btxtech.client.dialog.common.ScrollTipDialog;
import com.btxtech.client.dialog.common.SetUserNameDialog;
import com.btxtech.client.dialog.common.UserAccountDialog;
import com.btxtech.client.dialog.levelup.LevelUpDialog;
import com.btxtech.client.dialog.unlock.UnlockDialog;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.tip.tiptask.ScrollTipDialogModel;
import com.btxtech.uiservice.unlock.UnlockUiService;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    private Instance<UserUiService> userUiServicesInstance;
    @Inject
    private AudioService audioService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private UnlockUiService unlockUiService;
    private ModalDialogPanel activeDialog;
    private List<DialogParameters> dialogQueue = new ArrayList<>();
    private List<ModalDialogPanel> stackedDialogs = new ArrayList<>();
    private BiConsumer<ModalDialogPanel, Boolean> trackerCallback;

    @Override
    protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener) {
        show("Quest bestanden", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, QuestPassedDialog.class, questDescriptionConfig, (button, value) -> closeListener.run(), null, audioService.getAudioConfig().getOnQuestPassed(), DialogButton.Button.CLOSE);
    }

    @Override
    protected void showLevelUp(LevelUpPacket levelUpPacket, Runnable closeListener) {
        show(I18nHelper.getConstants().levelUpDialogTitle(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, LevelUpDialog.class, levelUpPacket, (button, value) -> {
            closeListener.run();
            if (unlockUiService.hasItems2Unlock()) {
                show(I18nHelper.getConstants().unlockDialogTitle(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, UnlockDialog.class, null, null, null, DialogButton.Button.CLOSE);
            }
        }, null, audioService.getAudioConfig().getOnLevelUp(), DialogButton.Button.CLOSE);
    }

    @Override
    public void showBoxPicked(BoxContent boxContent) {
        show("Box gesammelt", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BoxContentDialog.class, boxContent, null, null, audioService.getAudioConfig().getOnBoxPicked(), DialogButton.Button.CLOSE);
    }

    @Override
    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        show(I18nHelper.getConstants().useItem(), ClientModalDialogManagerImpl.Type.STACK_ABLE, MessageDialog.class, I18nHelper.getConstants().useItemLimit(I18nHelper.getLocalizedString(baseItemType.getI18nName())), null, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void showUseInventoryHouseSpaceExceeded() {
        show(I18nHelper.getConstants().useItem(), ClientModalDialogManagerImpl.Type.STACK_ABLE, MessageDialog.class, I18nHelper.getConstants().useItemHouseSpace(), null, null, DialogButton.Button.CLOSE);
    }

    @Override
    protected void showBaseLost(Runnable closeListener) {
        show(I18nHelper.getConstants().baseLostTitle(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, MessageDialog.class, I18nHelper.getConstants().baseLost(), (button, value) -> closeListener.run(), null, audioService.getAudioConfig().getOnBaseLost(), DialogButton.Button.CLOSE);
    }

    @Override
    public void showLeaveStartTutorial(Runnable closeListener) {
        show(I18nHelper.getConstants().nextPlanet(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, MessageDialog.class, I18nHelper.getConstants().startTutorialFinished(), (button, value) -> closeListener.run(), null, DialogButton.Button.OK);
    }

    @Override
    public void showMessageImageDialog(String title, String message, Integer imageId) {
        show(title, Type.STACK_ABLE, MessageImageDialog.class, new MessageImage(message, imageId), null, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void showRegisterDialog() {
        if (userUiServicesInstance.get().isRegistered()) {
            throw new IllegalStateException("User is already registered");
        }
        show(I18nHelper.getConstants().register(), Type.QUEUE_ABLE, RegisterDialog.class, null, null, null, DialogButton.Button.CANCEL);
    }

    @Override
    public void showSetUserNameDialog() {
        if (userUiServicesInstance.get().isRegisteredAndNamed()) {
            throw new IllegalStateException("User is already registered and named");
        }
        if (userUiServicesInstance.get().isEmailNotVerified()) {
            showMessageDialog(I18nHelper.getConstants().setName(), I18nHelper.getConstants().setNameFailedNotVerifiedText());
            return;
        }
        show(I18nHelper.getConstants().setName(), Type.QUEUE_ABLE, SetUserNameDialog.class, null, null, null, DialogButton.Button.CANCEL);
    }

    public void showUserAccountDialog() {
        if (!userUiServicesInstance.get().isRegistered()) {
            throw new IllegalStateException("User is not registered");
        }
        show(I18nHelper.getConstants().userAccountDialogTitle(), Type.QUEUE_ABLE, UserAccountDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }


    @Override
    public void showMessageDialog(String title, String message) {
        show(title, Type.STACK_ABLE, MessageDialog.class, message, null, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void showScrollTipDialog(ScrollTipDialogModel scrollTipDialogModel) {
        show(scrollTipDialogModel.getDialogTitle(), Type.STACK_ABLE, ScrollTipDialog.class, scrollTipDialogModel, null, null, DialogButton.Button.CLOSE);
    }

    public void showMessageNoClosableDialog(String title, String message, Consumer<ModalDialogPanel> shownCallback) {
        show(title, Type.STACK_ABLE, MessageDialog.class, message, null, shownCallback);
    }

    public void showSingleNoClosableDialog(String title, String message) {
        closeAll();
        show(title, Type.PROMPTLY, MessageDialog.class, message, null, null);
    }

    public void showQuestionDialog(String title, String question, Runnable okCallback, Runnable cancelCallback) {
        show(title, Type.STACK_ABLE, MessageDialog.class, question,
                (button, ignore) -> {
                    switch (button) {
                        case OK:
                            if (okCallback != null) {
                                okCallback.run();
                            }
                            break;
                        case CANCEL:
                            if (cancelCallback != null) {
                                cancelCallback.run();
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("ClientModalDialogManagerImpl.showQuestionDialog() Unknown button: " + button);
                    }
                }, null, DialogButton.Button.OK, DialogButton.Button.CANCEL);
    }

    public <T> void show(String title, Type type, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, Consumer<ModalDialogPanel> shownCallback, DialogButton.Button... dialogButtons) {
        show(title, type, contentClass, t, listener, shownCallback, audioService.getAudioConfig().getDialogOpened(), dialogButtons);
    }

    public <T> void show(String title, Type type, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, Consumer<ModalDialogPanel> shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
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

    private void showDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener listener, Consumer<ModalDialogPanel> shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
        try {
            ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
            modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, listener, dialogButtons);
            this.activeDialog = modalDialogPanel;
            showDialog(activeDialog, shownCallback, audioId);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    private void showStackedDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener listener, Consumer<ModalDialogPanel> shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
        try {
            ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
            modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, listener, dialogButtons);
            stackedDialogs.add(modalDialogPanel);
            showDialog(modalDialogPanel, shownCallback, audioId);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    private void showDialog(ModalDialogPanel modalDialogPanel, Consumer<ModalDialogPanel> shownCallback, Integer audioId) {
        audioService.onDialogOpened(audioId);
        RootPanel.get().add(modalDialogPanel);
        modalDialogPanel.onShown();
        if (shownCallback != null) {
            shownCallback.accept(modalDialogPanel);
        }
        if (trackerCallback != null) {
            trackerCallback.accept(modalDialogPanel, true);
        }
    }

    public void close(ModalDialogPanel modalDialogPanel) {
        audioService.onDialogClosed();
        modalDialogPanel.onClose();
        if (trackerCallback != null) {
            trackerCallback.accept(modalDialogPanel, false);
        }
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

    public void closeAll() {
        dialogQueue.clear();
        while (!stackedDialogs.isEmpty()) {
            close(stackedDialogs.get(0));
        }
        if (activeDialog != null) {
            close(activeDialog);
        }
    }

    public void setTrackerCallback(BiConsumer<ModalDialogPanel, Boolean> trackerCallback) {
        this.trackerCallback = trackerCallback;
    }

    private class DialogParameters {
        private String title;
        private Class<? extends ModalDialogContent> contentClass;
        private Object object;
        private DialogButton.Listener<?> listener;
        private Consumer<ModalDialogPanel> shownCallback;
        private Integer audioId;
        private DialogButton.Button[] dialogButtons;

        DialogParameters(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener<?> listener, Consumer<ModalDialogPanel> shownCallback, Integer audioId, DialogButton.Button... dialogButtons) {
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
