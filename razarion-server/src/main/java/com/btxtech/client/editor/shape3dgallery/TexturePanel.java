package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.widgets.ImageItemWidget;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.08.2016.
 */
@Templated("VertexContainerDialog.html#textures")
public class TexturePanel implements TakesValue<VertexContainer>, IsElement {
    // private Logger logger = Logger.getLogger(TexturePanel.class.getName());
    @Inject
    private Event<TexturePanel> eventTrigger;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("textures")
    private Div div;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label materialName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget imageItemWidget;
    private VertexContainer vertexContainer;
    private Integer newImageId;

    @Override
    public HTMLElement getElement() {
        return div;
    }

    @Override
    public void setValue(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
        materialName.setText(vertexContainer.getMaterialName());
        imageItemWidget.setImageId(vertexContainer.getTextureId(), imageId -> {
            newImageId = imageId;
            eventTrigger.fire(TexturePanel.this);
        });
    }

    @Override
    public VertexContainer getValue() {
        return vertexContainer;
    }

    public Integer getNewImageId() {
        return newImageId;
    }
}
