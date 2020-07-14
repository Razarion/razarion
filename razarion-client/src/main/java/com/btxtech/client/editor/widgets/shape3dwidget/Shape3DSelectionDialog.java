package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.Shape3DUiService;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("Shape3DSelectionDialog.html#shape3d-selection-dialog")
public class Shape3DSelectionDialog extends Composite implements ModalDialogContent<Integer> {
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
    private ListComponent<Shape3D, Shape3DSelectionEntry> shape3Ds;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(shape3Ds.getElement()); // Remove placeholder table row from template.
        // TODO get from server binder.setModel(shape3DUiService.getShape3Ds());
        shape3Ds.setSelector(shape3DSelectionEntry -> shape3DSelectionEntry.setSelected(true));
        shape3Ds.setDeselector(shape3DSelectionEntry -> shape3DSelectionEntry.setSelected(false));
        if (selectedId != null) {
            shape3Ds.selectModel(shape3DUiService.getShape3D(selectedId));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(@Observes Shape3DSelectionEntry widget) {
        if (isAttached()) {
            shape3Ds.deselectAll();
            shape3Ds.selectComponent(widget);
            modalDialogPanel.setApplyValue(widget.getValue().getId());
        }
    }

    @Override
    public void onClose() {

    }
}
