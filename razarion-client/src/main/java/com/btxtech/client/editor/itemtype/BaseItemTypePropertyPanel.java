package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.childpanel.ChildContainer;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("BaseItemTypePropertyPanel.html#sync-base-item-property-panel")
public class BaseItemTypePropertyPanel extends AbstractPropertyPanel<BaseItemType> {
    @Inject
    private BaseItemRenderTask baseItemRenderTask;
    @Inject
    @AutoBound
    private DataBinder<BaseItemType> baseItemTypeDataBinder;
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
    private Shape3DReferenceFiled spawnShape3DIdReferenceFiled;
    @Inject
    @DataField
    private ChildContainer<ConsumerType> consumerTypePanelChildTable;
    @Inject
    @DataField
    private ChildContainer<BuilderType> builderTypeChildContainer;
    @Inject
    @DataField
    private ChildContainer<FactoryType> factoryTypeChildContainer;
    @Inject
    @DataField
    private ChildContainer<GeneratorType> generatorTypeChildContainer;
    @Inject
    @DataField
    private ChildContainer<HarvesterType> harvesterTypeChildContainer;
    @Inject
    @DataField
    private ChildContainer<WeaponType> weaponTypeChildContainer;

    @Override
    public void init(BaseItemType baseItemType) {
        baseItemTypeDataBinder.setModel(baseItemType);
        shape3DReferenceFiled.init(baseItemType.getShape3DId(), shape3DId -> {
            baseItemType.setShape3DId(shape3DId);
            baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
        });
        spawnShape3DIdReferenceFiled.init(baseItemType.getSpawnShape3DId(), shape3DId -> {
            baseItemType.setSpawnShape3DId(shape3DId);
            baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
        });
        builderTypeChildContainer.init(baseItemType.getBuilderType(), baseItemType::setBuilderType, BuilderType::new, BuilderTypePanel.class);
        factoryTypeChildContainer.init(baseItemType.getFactoryType(), baseItemType::setFactoryType, FactoryType::new, FactoryTypePanel.class);
        harvesterTypeChildContainer.init(baseItemType.getHarvesterType(), baseItemType::setHarvesterType, HarvesterType::new, HarvesterTypePanel.class);
        weaponTypeChildContainer.init(baseItemType.getWeaponType(), baseItemType::setWeaponType, WeaponType::new, WeaponTypePanel.class);
        generatorTypeChildContainer.init(baseItemType.getGeneratorType(), baseItemType::setGeneratorType, GeneratorType::new, GeneratorTypePanel.class);
        consumerTypePanelChildTable.init(baseItemType.getConsumerType(), baseItemType::setConsumerType, ConsumerType::new, ConsumerTypePanel.class);
    }

    @Override
    public BaseItemType getConfigObject() {
        return baseItemTypeDataBinder.getModel();
    }
}
