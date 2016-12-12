package com.btxtech.client.dialog.framework;

import com.btxtech.client.cockpit.BoxContentDialog;
import com.btxtech.client.cockpit.level.LevelUpDialog;
import com.btxtech.client.cockpit.quest.QuestPassedDialog;
import com.btxtech.client.dialog.common.MessageDialog;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.dialog.AbstractModalDialogManager;
import com.btxtech.uiservice.dialog.ApplyListener;
import com.btxtech.uiservice.dialog.DialogButton;
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
public class ClientModalDialogManagerImpl extends AbstractModalDialogManager {
    public enum Type {
        PROMPTLY,
        STACK_ABLE,
        QUEUE_ABLE,
        UNIMPORTANT
    }

    @Inject
    private Instance<ModalDialogPanel<Object>> containerInstance;
    private ModalDialogPanel activeDialog;
    private List<DialogParameters> dialogQueue = new ArrayList<>();
    private List<ModalDialogPanel> stackedDialogs = new ArrayList<>();

    @Override
    protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener) {
        show("Quest bestanden", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, QuestPassedDialog.class, questDescriptionConfig, (button, value) -> closeListener.run(), DialogButton.Button.CLOSE);
    }

    @Override
    protected void showLevelUp(UserContext userContext, Runnable closeListener) {
        show("Level Up", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, LevelUpDialog.class, null, (button, value) -> closeListener.run(), DialogButton.Button.CLOSE);
    }

    @Override
    public void showBoxPicked(BoxContent boxContent) {
        show("Box gesammelt", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BoxContentDialog.class, boxContent, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        show(I18nHelper.getConstants().useItem(), ClientModalDialogManagerImpl.Type.STACK_ABLE, MessageDialog.class, I18nHelper.getConstants().useItemLimit(I18nHelper.getLocalizedString(baseItemType.getI18Name())), null, DialogButton.Button.CLOSE);
    }

    @Override
    public void showUseInventoryHouseSpaceExceeded() {
        show(I18nHelper.getConstants().useItem(), ClientModalDialogManagerImpl.Type.STACK_ABLE, MessageDialog.class, I18nHelper.getConstants().useItemHouseSpace(), null, DialogButton.Button.CLOSE);
    }

    public <T> void show(String title, Type type, Class<? extends ModalDialogContent<T>> contentClass, T t, DialogButton.Listener<T> listener, DialogButton.Button... dialogButtons) {
        if (activeDialog == null) {
            showDialog(title, contentClass, t, listener, dialogButtons);
        } else {
            switch (type) {
                case PROMPTLY:
                    close(activeDialog);
                    showDialog(title, contentClass, t, listener, dialogButtons);
                    break;
                case QUEUE_ABLE:
                    dialogQueue.add(new DialogParameters(title, contentClass, t, listener, dialogButtons));
                    break;
                case STACK_ABLE:
                    showStackedDialog(title, contentClass, t, listener, dialogButtons);
                    break;
                case UNIMPORTANT:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown dialog type: " + type);
            }
        }
    }

    private void showDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener listener, DialogButton.Button... dialogButtons) {
        ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
        modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, listener, dialogButtons);
        this.activeDialog = modalDialogPanel;
        RootPanel.get().add(activeDialog);
    }

    private void showStackedDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener listener, DialogButton.Button... dialogButtons) {
        ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
        modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, listener, dialogButtons);
        stackedDialogs.add(modalDialogPanel);
        RootPanel.get().add(modalDialogPanel);
    }

    public void close(ModalDialogPanel modalDialogPanel) {
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
        private DialogButton.Button[] dialogButtons;

        DialogParameters(String title, Class<? extends ModalDialogContent> contentClass, Object object, DialogButton.Listener<?> listener, DialogButton.Button... dialogButtons) {
            this.title = title;
            this.contentClass = contentClass;
            this.object = object;
            this.listener = listener;
            this.dialogButtons = dialogButtons;
        }

        void showDialog() {
            ClientModalDialogManagerImpl.this.showDialog(title, contentClass, object, listener, dialogButtons);
        }
    }
}
