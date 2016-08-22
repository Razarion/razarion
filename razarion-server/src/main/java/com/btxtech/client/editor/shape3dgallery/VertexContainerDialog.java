package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.utils.Shape3DUtils;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("VertexContainerDialog.html#vertex-container-widget")
public class VertexContainerDialog extends Composite implements ModalDialogContent<Shape3D> {
    // private Logger logger = Logger.getLogger(VertexContainerDialog.class.getName());
    @Inject
    @AutoBound
    private DataBinder<List<VertexContainer>> binder;
    @Inject
    private Shape3DCrud shape3DCrud;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private ListComponent<VertexContainer, TexturePanel> texturesWidget;
    private Shape3D shape3D;
    private boolean isClosed = false;

    @Override
    public void init(Shape3D shape3D) {
        DOMUtil.removeAllElementChildren(texturesWidget.getElement()); // Remove placeholder table row from template.
        this.shape3D = shape3D;
        binder.setModel(Shape3DUtils.getAllVertexContainers(shape3D));
    }

    @Override
    public void onClose() {
        isClosed = true;
    }

    @Override
    public void customize(ModalDialogPanel<Shape3D> modalDialogPanel) {
        modalDialogPanel.showApplyButton(false);
        modalDialogPanel.showCancelButton(false);
    }

    public void onTextureIdChanged(@Observes TexturePanel texturePanel) {
        if (isClosed) {
            return;
        }
        if (binder.getModel().contains(texturePanel.getValue())) {
            shape3DCrud.updateTexture(shape3D, texturePanel.getValue().getMaterialId(), texturePanel.getNewImageId());
        }
    }
}
