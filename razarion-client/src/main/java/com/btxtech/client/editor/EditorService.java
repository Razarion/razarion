package com.btxtech.client.editor;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.AlarmServiceView.ClientAlarmView;
import com.btxtech.client.editor.AlarmServiceView.ServerAlarmView;
import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.client.editor.editorpanel.EditorPanel;
import com.btxtech.client.editor.generic.custom.CustomWidget;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.dialog.DialogButton;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class EditorService {
    private Logger logger = Logger.getLogger(EditorService.class.getName());
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private Instance<EditorPanel> editorPanelInstance;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void openEditorMenu() {
        try {
            modalDialogManager.show("Editor Menu",
                    ClientModalDialogManagerImpl.Type.QUEUE_ABLE,
                    EditorMenuDialog.class,
                    null,
                    null,
                    null,
                    (Integer) null,
                    DialogButton.Button.CLOSE);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void activateFallbackEditorMenuButton() {
        HTMLButtonElement buttonElement = (HTMLButtonElement) DomGlobal.document.createElement("button");
        buttonElement.innerHTML = "Editor Menu";
        buttonElement.addEventListener("click", evt -> {
            openEditorMenu();

        });
        mainPanelService.addToGamePanel(buttonElement);
        openEditorMenu();
    }

    public void openClientAlarmView() {
        openEditor(ClientAlarmView.class, "Client Alarms");
    }

    public void openServerAlarmView() {
        openEditor(ServerAlarmView.class, "Server Alarms");
    }


    public void openEditor(Class<? extends AbstractEditor> editorPanelClass, String title) {
        try {
            EditorPanel editorPanel = editorPanelInstance.get();
            editorPanel.setContent(editorPanelClass, title);
            mainPanelService.addEditorPanel(editorPanel);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            modalDialogManager.showMessageDialog("Error open Editor", t.getMessage());
        }
    }

    public void openGenericCrudEditor(Class<? extends CrudController> crudControllerClass, String title, Class<? extends CustomWidget> customWidgetClass) {
        try {
            EditorPanel editorPanel = editorPanelInstance.get();
            editorPanel.setGenericCrud(crudControllerClass, title, customWidgetClass);
            mainPanelService.addEditorPanel(editorPanel);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            modalDialogManager.showMessageDialog("Error open Editor", t.getMessage());
        }
    }
}
