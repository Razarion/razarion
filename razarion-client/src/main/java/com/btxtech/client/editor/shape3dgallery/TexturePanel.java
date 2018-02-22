package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.widgets.ColorRoWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 19.08.2016.
 */
@Templated("Shape3DPropertyPanel.html#tableRowTextures")
public class TexturePanel implements TakesValue<VertexContainer>, IsElement {
    // private Logger logger = Logger.getLogger(TexturePanel.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private TableRow tableRowTextures;
    @Inject
    @DataField
    private Label materialName;
    @Inject
    @DataField
    private ImageItemWidget imageItemWidget;
    @Inject
    @DataField
    private CheckboxInput characterRepresentingCheckbox;
    @Inject
    @DataField
    private ColorRoWidget ambient;
    @Inject
    @DataField
    private ColorRoWidget diffuse;
    private VertexContainer vertexContainer;
    private Integer newImageId;
    private Shape3DPropertyPanel shape3DPropertyPanel;

    @Override
    public HTMLElement getElement() {
        return tableRowTextures;
    }

    @Override
    public void setValue(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
        materialName.setText(vertexContainer.getMaterialName() + "(" + vertexContainer.getMaterialId() + ")");
        imageItemWidget.setImageId(vertexContainer.getTextureId(), imageId -> {
            newImageId = imageId;
            if (vertexContainer.getMaterialId() != null) {
                try {
                    shape3DPropertyPanel.textureIdChanged(this);
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        });
        characterRepresentingCheckbox.setChecked(vertexContainer.isCharacterRepresenting());
        ambient.init(vertexContainer.getAmbient());
        diffuse.init(vertexContainer.getDiffuse());
    }

    @Override
    public VertexContainer getValue() {
        return vertexContainer;
    }

    public Integer getNewImageId() {
        return newImageId;
    }

    public void setShape3DPropertyPanel(Shape3DPropertyPanel shape3DPropertyPanel) {
        this.shape3DPropertyPanel = shape3DPropertyPanel;
    }

    @EventHandler("characterRepresentingCheckbox")
    public void characterRepresentingClicked(ChangeEvent event) {
        try {
            shape3DPropertyPanel.characterRepresentingChanged(this);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public boolean isCharacterRepresenting() {
        return characterRepresentingCheckbox.getChecked();
    }
}
