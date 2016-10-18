package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 19.08.2016.
 */
@Templated("Shape3DPropertyPanel.html#tableRowTextures")
public class TexturePanel implements TakesValue<VertexContainer>, IsElement {
    // private Logger logger = Logger.getLogger(TexturePanel.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private TableRow tableRowTextures;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label materialName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget imageItemWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget lookUpTextureId;
    private VertexContainer vertexContainer;
    private Integer newImageId;
    private Integer newLookUpTextureId;
    private Shape3DPropertyPanel shape3DPropertyPanel;

    @Override
    public HTMLElement getElement() {
        return tableRowTextures;
    }

    @Override
    public void setValue(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
        materialName.setText(vertexContainer.getMaterialName());
        imageItemWidget.setImageId(vertexContainer.getTextureId(), imageId -> {
            newImageId = imageId;
            if (vertexContainer.getMaterialId() != null) {
                shape3DPropertyPanel.textureIdChanged(this);
            }
        });
        lookUpTextureId.setImageId(vertexContainer.getLookUpTextureId(), imageId -> {
            newLookUpTextureId = imageId;
            if (vertexContainer.getMaterialId() != null) {
                shape3DPropertyPanel.lookUpTextureIdChanged(this);
            }
        });
    }

    @Override
    public VertexContainer getValue() {
        return vertexContainer;
    }

    public Integer getNewImageId() {
        return newImageId;
    }

    public Integer getNewLookUpTextureId() {
        return newLookUpTextureId;
    }

    public void setShape3DPropertyPanel(Shape3DPropertyPanel shape3DPropertyPanel) {
        this.shape3DPropertyPanel = shape3DPropertyPanel;
    }
}
