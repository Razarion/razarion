package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.Shape3DUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
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
public class Shape3DReferenceFiled extends Composite {
    // private Logger logger = Logger.getLogger(Shape3DReferenceFiled.class.getName());
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private Shape3DUiService shape3DUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button galleryButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label nameLabel;
    private Integer shape3DId;
    private Consumer<Integer> shape3DIdConsumer;

    public void init(Integer shape3DId, Consumer<Integer> shape3DIdConsumer) {
        this.shape3DId = shape3DId;
        this.shape3DIdConsumer = shape3DIdConsumer;
        if (shape3DId != null) {
            shape3DUiService.request(shape3DId, this::displayShape3D, false);
        }
    }

    @EventHandler("galleryButton")
    private void galleryButtonClicked(ClickEvent event) {
        modalDialogManager.show("Shape 3D Gallery", ModalDialogManager.Type.QUEUE_ABLE, Shape3DSelectionDialog.class, shape3DId, selectedId -> {
            shape3DId = selectedId;
            shape3DIdConsumer.accept(shape3DId);
            shape3DUiService.request(shape3DId, this::displayShape3D, false);
        });
    }

    private void displayShape3D(Shape3D shape3D) {
        nameLabel.setText(shape3D.getInternalName() + "(" + shape3D.getDbId() + ")");
    }
}
