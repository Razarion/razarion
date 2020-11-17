package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("Shape3DReferenceFiled.html#field")
public class Shape3DReferenceFiled implements IsElement {
    // private Logger logger = Logger.getLogger(Shape3DReferenceFiled.class.getName());
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private Caller<Shape3DEditorController> shape3DEditorControllerCaller;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    @DataField
    private HTMLDivElement field;
    @Inject
    @DataField
    private HTMLButtonElement galleryButton;
    @Inject
    @DataField
    private HTMLDivElement nameLabel;
    private Integer shape3DId;
    private Consumer<Integer> shape3DIdConsumer;

    public void init(Integer shape3DId, Consumer<Integer> shape3DIdConsumer) {
        this.shape3DId = shape3DId;
        this.shape3DIdConsumer = shape3DIdConsumer;
        setupNameLabel(shape3DId);
    }

    @EventHandler("galleryButton")
    private void galleryButtonClicked(ClickEvent event) {
        modalDialogManager.show("Shape 3D Gallery", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, Shape3DSelectionDialog.class, shape3DId, (button, selectedId) -> {
            if (button == DialogButton.Button.APPLY) {
                shape3DId = selectedId;
                shape3DIdConsumer.accept(shape3DId);
                setupNameLabel(shape3DId);
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    private void setupNameLabel(Integer shape3DId) {
        if (shape3DId != null) {
            shape3DEditorControllerCaller.call(
                    (RemoteCallback<Shape3DConfig>) shape3DConfig -> nameLabel.textContent = shape3DConfig.getInternalName() + " (" + shape3DConfig.getId() + ")",
                    exceptionHandler.restErrorHandler("Shape3DEditorController.read()")
            ).read(shape3DId);
        } else {
            nameLabel.textContent = "-";
        }
    }

    @Override
    public HTMLElement getElement() {
        return field;
    }
}
