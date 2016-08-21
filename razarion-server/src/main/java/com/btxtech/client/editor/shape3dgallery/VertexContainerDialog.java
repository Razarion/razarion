package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.utils.Shape3DUtils;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("VertexContainerDialog.html#vertex-container-widget")
public class VertexContainerDialog extends Composite implements ModalDialogContent<Shape3D> {
    // private Logger logger = Logger.getLogger(VertexContainerDialog.class.getName());
//    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    private RenderService renderService;
    @Inject
    @AutoBound
    private DataBinder<List<VertexContainer>> binder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private ListComponent<VertexContainer, TexturePanel> texturesWidget;

    @Override
    public void init(Shape3D shape3D) {
        binder.setModel(Shape3DUtils.getAllVertexContainers(shape3D));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void customize(ModalDialogPanel<Shape3D> modalDialogPanel) {
        modalDialogPanel.showApplyButton(false);
        modalDialogPanel.showCancelButton(false);
    }
}
