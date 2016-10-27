package com.btxtech.client.dialog;

import com.btxtech.client.cockpit.BoxContentDialog;
import com.btxtech.client.cockpit.level.LevelUpDialog;
import com.btxtech.client.cockpit.quest.QuestPassedDialog;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.uiservice.dialog.ApplyListener;
import com.btxtech.uiservice.dialog.AbstractModalDialogManager;
import com.google.gwt.user.client.ui.RootPanel;

import javax.annotation.PostConstruct;
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


//    private ModalDialogPanel container;
//    private ModalDialogContent content;
//    private ApplyListener applyListener;

    @Override
    protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, ApplyListener<QuestDescriptionConfig> applyListener) {
        show("Quest bestanden", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, QuestPassedDialog.class, questDescriptionConfig, applyListener);
    }

    @Override
    protected void showLevelUp(UserContext userContext, ApplyListener<Void> applyListener) {
        show("Level Up", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, LevelUpDialog.class, null, applyListener);
    }

    @Override
    public void showBoxPicked(BoxContent boxContent) {
        show("Level Up", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BoxContentDialog.class, boxContent, null);
    }

    public <T> void show(String title, Type type, Class<? extends ModalDialogContent<T>> contentClass, T t, ApplyListener<T> applyListener) {
        if (activeDialog == null) {
            showDialog(title, contentClass, t, applyListener);
        } else {
            switch (type) {
                case PROMPTLY:
                    close(activeDialog);
                    showDialog(title, contentClass, t, applyListener);
                    break;
                case QUEUE_ABLE:
                    dialogQueue.add(new DialogParameters(title, contentClass, t, applyListener));
                    break;
                case STACK_ABLE:
                    showStackedDialog(title, contentClass, t, applyListener);
                    break;
                case UNIMPORTANT:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown dialog type: " + type);
            }
        }
    }

    private void showDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, ApplyListener applyListener) {
        ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
        modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, applyListener);
        this.activeDialog = modalDialogPanel;
        RootPanel.get().add(activeDialog);
    }

    private void showStackedDialog(String title, Class<? extends ModalDialogContent> contentClass, Object object, ApplyListener applyListener) {
        ModalDialogPanel<Object> modalDialogPanel = containerInstance.get();
        modalDialogPanel.init(title, (Class<? extends ModalDialogContent<Object>>) contentClass, object, applyListener);
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
        private ApplyListener<?> applyListener;

        public DialogParameters(String title, Class<? extends ModalDialogContent> contentClass, Object object, ApplyListener<?> applyListener) {
            this.title = title;
            this.contentClass = contentClass;
            this.object = object;
            this.applyListener = applyListener;
        }

        public void showDialog() {
            ClientModalDialogManagerImpl.this.showDialog(title, contentClass, object, applyListener);
        }
    }
}
