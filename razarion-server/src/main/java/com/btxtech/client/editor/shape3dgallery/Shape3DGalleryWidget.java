package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 17.08.2016.
 */
@Templated("Shape3DGalleryDialog.html#tableRow")
public class Shape3DGalleryWidget implements TakesValue<Shape3D>, IsElement {
    // private Logger logger = Logger.getLogger(Shape3DGalleryWidget.class.getName());
    @Inject
    private ModalDialogManager modalDialogManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Shape3DCrud shape3DCrud;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private TableRow tableRow;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label dbId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label internalName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button textureButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button animationButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button uploadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button deleteButton;
    @Inject
    private Event<Shape3DGalleryWidget> eventTrigger;
    private Shape3D shape3D;

    @Override
    public void setValue(Shape3D shape3D) {
        this.shape3D = shape3D;
        dbId.setText(DisplayUtils.handleInteger(shape3D.getDbId()));
        internalName.setText(shape3D.getInternalName());
    }

    @Override
    public Shape3D getValue() {
        return shape3D;
    }

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @EventHandler("textureButton")
    private void onTextureButtonClicked(ClickEvent event) {
        modalDialogManager.show("Textures", ModalDialogManager.Type.STACK_ABLE, VertexContainerDialog.class, shape3D, null);
    }

    @EventHandler("deleteButton")
    private void deleteButtonClicked(ClickEvent event) {
        shape3DCrud.delete(shape3D);
    }

    @EventHandler("animationButton")
    private void onAnimationButtonClicked(ClickEvent event) {
        // TODO hier
    }

    @EventHandler("uploadButton")
    private void onUploadButtonClicked(ClickEvent event) {
        ControlUtils.openSingleFileTextUpload((colladaText, file) -> shape3DCrud.updateCollada(shape3D, colladaText));
    }

    @EventHandler("tableRow")
    public void onClick(final ClickEvent event) {
        eventTrigger.fire(this);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-not-selected");
        } else {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-not-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-selected");
        }
    }
}
