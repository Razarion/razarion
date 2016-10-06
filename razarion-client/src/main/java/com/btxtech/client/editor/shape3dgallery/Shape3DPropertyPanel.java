package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.Shape3DUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import elemental.html.File;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("Shape3DPropertyPanel.html#shape3d-property-panel")
public class Shape3DPropertyPanel extends AbstractPropertyPanel<Shape3D> {
    // private Logger logger = Logger.getLogger(Shape3DPropertyPanel.class.getName());
    @Inject
    private Shape3DCrud shape3DCrud;
    @Inject
    private Shape3DUiService shape3DUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<VertexContainer, TexturePanel> textures;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label dbId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label internalName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button selectFileButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadFileButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label loadedTimestamp;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label fileTimestamp;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ModelMatrixAnimation, AnimationPanel> animations;
    private File file;
    private Shape3D shape3D;

    @Override
    public void init(Shape3D shape3D) {
        dbId.setText(DisplayUtils.handleInteger(shape3D.getDbId()));
        internalName.setText(shape3D.getInternalName());
        textures.addComponentCreationHandler(texturePanel -> texturePanel.setShape3DPropertyPanel(this));
        animations.addComponentCreationHandler(animationPanel -> animationPanel.setShape3DPropertyPanel(this));
        DOMUtil.removeAllElementChildren(textures.getElement()); // Remove placeholder table row from template.
        DOMUtil.removeAllElementChildren(animations.getElement()); // Remove placeholder table row from template.
        onChange(shape3D);
    }

    @Override
    public Shape3D getConfigObject() {
        return shape3D;
    }

    @Override
    public void onChange(Shape3D shape3D) {
        this.shape3D = shape3D;
        internalName.setText(shape3D.getInternalName());
        textures.setValue(Shape3DUtils.getAllVertexContainers(shape3D));
        if(shape3D.getModelMatrixAnimations() != null) {
            animations.setValue(shape3D.getModelMatrixAnimations());
        } else {
            animations.setValue(Collections.emptyList());
        }
    }

    @EventHandler("selectFileButton")
    private void selectFileButtonClicked(ClickEvent event) {
        ControlUtils.openSingleFileTextUpload((colladaText, file) -> {
            shape3DCrud.updateCollada(shape3D, colladaText);
            this.file = file;
            reloadFileButton.setEnabled(true);
            loadedTimestamp.setText(DisplayUtils.formatDate(new Date()));
            fileTimestamp.setText(DisplayUtils.formatDate(getLastModifiedDate(file)));
        });
    }

    @EventHandler("reloadFileButton")
    private void reloadFileButtonClick(ClickEvent event) {
        if (file != null) {
            ControlUtils.readFileText(file, colladaText -> {
                shape3DCrud.updateCollada(shape3D, colladaText);
                loadedTimestamp.setText(DisplayUtils.formatDate(new Date()));
                fileTimestamp.setText(DisplayUtils.formatDate(getLastModifiedDate(file)));
            });
        }
    }

    public void texturePanelChanged(TexturePanel texturePanel) {
        if (Shape3DUtils.getAllVertexContainers(shape3D).contains(texturePanel.getValue())) {
            shape3DCrud.updateTexture(shape3D, texturePanel.getValue().getMaterialId(), texturePanel.getNewImageId());
        }
    }

    public void animationPanelChanged(AnimationPanel animationPanel) {
        if (shape3D.getModelMatrixAnimations().contains(animationPanel.getValue())) {
            shape3DCrud.updateAnimation(shape3D, animationPanel.getValue().getId(), animationPanel.getNewItemState());
        }
    }

    private Date getLastModifiedDate(File file) {
        // Unfortunately, the GWT elemental has trouble with the date
        return new Date(Long.parseLong(getLastModifiedDateAsLongString(file)));
    }

    private native String getLastModifiedDateAsLongString(File file) /*-{
        // Unfortunately long can not be returned
        return file.lastModifiedDate.getTime() + "";
    }-*/;
}
