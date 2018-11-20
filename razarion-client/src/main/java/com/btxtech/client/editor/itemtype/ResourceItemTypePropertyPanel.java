package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.I18nStringWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.renderer.task.ResourceItemRenderTask;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("ResourceItemTypePropertyPanel.html#resource-item-property-panel")
public class ResourceItemTypePropertyPanel extends AbstractPropertyPanel<ResourceItemType> {
    @Inject
    private ResourceItemRenderTask resourceItemRenderTask;
    @Inject
    @AutoBound
    private DataBinder<ResourceItemType> resourceItemTypeDataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DReferenceFiled;
    @Inject
    @DataField
    private I18nStringWidget i18nName;
    @Inject
    @DataField
    private I18nStringWidget i18nDescription;
    @Inject
    @DataField
    private ImageItemWidget thumbnail;
    @Inject
    @DataField
    private ValueListBox<TerrainType> terrainType;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox radius;
    @Inject
    @Bound
    @DataField
    private CheckboxInput fixVerticalNorm;
    @Inject
    @Bound
    @DataField
    private IntegerBox amount;

    @Override
    public void init(ResourceItemType resourceItemType) {
        resourceItemTypeDataBinder.setModel(resourceItemType);
        shape3DReferenceFiled.init(resourceItemType.getShape3DId(), shape3DId -> {
            resourceItemType.setShape3DId(shape3DId);
            resourceItemRenderTask.onResourceItemTypeChanged(resourceItemType);
        });
        i18nName.init(resourceItemType.getI18nName(), resourceItemType::setI18nName);
        i18nDescription.init(resourceItemType.getI18nDescription(), resourceItemType::setI18nDescription);
        thumbnail.setImageId(resourceItemType.getThumbnail(), resourceItemType::setThumbnail);
        terrainType.setAcceptableValues(Arrays.asList(TerrainType.values()));
        terrainType.setValue(resourceItemType.getTerrainType());
        terrainType.addValueChangeHandler(event -> {
            resourceItemType.setTerrainType(terrainType.getValue());
        });
    }

    @Override
    public ResourceItemType getConfigObject() {
        return resourceItemTypeDataBinder.getModel();
    }
}
