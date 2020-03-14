package com.btxtech.client.editor;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.dialog.DialogButton;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;

import javax.inject.Inject;
import java.util.logging.Logger;

public class EditorFallback {
    private Logger logger = Logger.getLogger(EditorFallback.class.getName());
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void activateButton() {
        logger.severe("Using Fallback. Show Editor Menu.");
        HTMLButtonElement buttonElement = (HTMLButtonElement) DomGlobal.document.createElement("button");
        buttonElement.innerHTML = "Editor Menu";
        buttonElement.addEventListener("click", evt -> {
            openEditorMenu();

        });
        mainPanelService.addToGamePanel(buttonElement);
        openEditorMenu();
    }

    private void openEditorMenu() {
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
}
