package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
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
    private Caller<Shape3DEditorController> shape3DEditorControllerCaller;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    @AutoBound
    private DataBinder<List<Shape3DConfig>> binder;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<Shape3DConfig, Shape3DSelectionEntry> shape3Ds;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(shape3Ds.getElement()); // Remove placeholder table row from template.
        shape3DEditorControllerCaller.call(
                (RemoteCallback<List<Shape3DConfig>>) shape3DConfigs -> {
                    binder.setModel(shape3DConfigs);
                    if (selectedId != null) {
                        shape3Ds.selectModel(getShape3DConfig4Id(shape3DConfigs, selectedId));
                    }
                },
                exceptionHandler.restErrorHandler("Shape3DEditorController.read()")
        ).read();
        shape3Ds.setSelector(shape3DSelectionEntry -> shape3DSelectionEntry.setSelected(true));
        shape3Ds.setDeselector(shape3DSelectionEntry -> shape3DSelectionEntry.setSelected(false));
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

    private Shape3DConfig getShape3DConfig4Id(List<Shape3DConfig> shape3DConfigs, int id) {
        return shape3DConfigs.stream().filter(shape3DConfig -> shape3DConfig.getId() == id).findFirst().orElseThrow(() -> new IllegalArgumentException("No Shape3DConfig for id: " + id));
    }
}
