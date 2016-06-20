package com.btxtech.client.sidebar.colladaeditor;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.widgets.ImageItemWidget;
import com.btxtech.shared.dto.VertexContainer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("VertexContainerWidget.html#vertexContainerWidget")
public class VertexContainerWidget extends Composite implements HasModel<VertexContainer>, ImageItemWidget.ImageItemWidgetListener {
    // private Logger logger = Logger.getLogger(VertexContainerWidget.class.getName());
    @Inject
    private RenderService renderService;
    @Inject
    @DataField
    private ImageItemWidget imageItemWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label materialName;
    private VertexContainer vertexContainer;

    @Override
    public VertexContainer getModel() {
        return vertexContainer;
    }

    @Override
    public void setModel(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
        materialName.setText(vertexContainer.getMaterialName());
        imageItemWidget.setImageId(vertexContainer.getTextureId(), this);
    }

    @Override
    public void onIdChanged(int id) {
        vertexContainer.setTextureId(id);
        renderService.fillBuffers();
    }
}
