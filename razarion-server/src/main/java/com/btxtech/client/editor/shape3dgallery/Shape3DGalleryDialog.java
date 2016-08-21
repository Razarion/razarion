package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.Shape3DUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 17.08.2016.
 */
@Templated("Shape3DGalleryDialog.html#shape3d-gallery-dialog")
public class Shape3DGalleryDialog extends Composite implements ModalDialogContent<Integer> {
    // private Logger logger = Logger.getLogger(Shape3DGalleryDialog.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    @AutoBound
    private DataBinder<List<Shape3D>> binder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<Shape3D, Shape3DGalleryWidget> shape3Ds;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button saveButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button newButton;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        shape3Ds.setSelector(shape3DGalleryWidget -> shape3DGalleryWidget.setSelected(true));
        shape3Ds.setDeselector(shape3DGalleryWidget -> shape3DGalleryWidget.setSelected(false));
        fill(shape3DUiService.getAllShape3Ds());
        if(selectedId != null) {
            shape3DUiService.request(selectedId, shape3Ds::selectModel, false);
        }
    }

    @EventHandler("newButton")
    private void newButtonClicked(ClickEvent event) {
        ControlUtils.openSingleFileTextUpload((dataUrl, file) -> shape3DUiService.create(dataUrl, this::fill));
    }

    private void fill(List<Shape3D> allShape3Ds) {
        // Remove placeholder table row from template.
        DOMUtil.removeAllElementChildren(shape3Ds.getElement());
        binder.setModel(allShape3Ds);
    }

    public void selectComponent(final @Observes Shape3DGalleryWidget widget) {
        shape3Ds.deselectAll();
        shape3Ds.selectComponent(widget);
        modalDialogPanel.setApplyValue(widget.getValue().getDbId());
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @Override
    public void onClose() {

    }
}
