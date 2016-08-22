package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.dialog.ModalDialogContent;
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
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 17.08.2016.
 */
@Templated("Shape3DGalleryDialog.html#shape3d-gallery-dialog")
public class Shape3DGalleryDialog extends Composite implements ModalDialogContent<Integer> {
    // private Logger logger = Logger.getLogger(Shape3DGalleryDialog.class.getName());
    @Inject
    private Shape3DCrud shape3DCrud;
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
        DOMUtil.removeAllElementChildren(shape3Ds.getElement()); // Remove placeholder table row from template.
        shape3Ds.setSelector(shape3DGalleryWidget -> shape3DGalleryWidget.setSelected(true));
        shape3Ds.setDeselector(shape3DGalleryWidget -> shape3DGalleryWidget.setSelected(false));
        shape3DCrud.monitor((allShape3Ds) -> {
            fill(allShape3Ds);
            if (selectedId != null) {
                shape3DUiService.request(selectedId, shape3Ds::selectModel, false);
            }
        });
    }

    @EventHandler("reloadButton")
    private void reloadButtonClicked(ClickEvent event) {
        shape3DCrud.reload();
    }

    @EventHandler("newButton")
    private void newButtonClicked(ClickEvent event) {
        ControlUtils.openSingleFileTextUpload((dataUrl, file) -> shape3DCrud.create(dataUrl));
    }

    @EventHandler("saveButton")
    private void saveButtonClicked(ClickEvent event) {
        shape3DCrud.save();
    }

    private void fill(List<Shape3D> allShape3Ds) {
        Collection<Shape3D> selection = shape3Ds.getSelectedModels();
        binder.setModel(allShape3Ds);
        if (selection != null && !selection.isEmpty()) {
            shape3Ds.selectModels(selection);
        }
    }

    public void selectComponent(@Observes Shape3DGalleryWidget widget) {
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
        shape3DCrud.removeMonitor(this::fill);
    }
}
