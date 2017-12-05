package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.I18nStringWidget;
import com.btxtech.client.editor.widgets.audio.AudioWidget;
import com.btxtech.client.editor.widgets.childpanel.ChildContainer;
import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.editor.widgets.itemtype.box.BoxItemTypeWidget;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpecialType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
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
    private I18nStringWidget i18nName;
    @Inject
    @DataField
    private I18nStringWidget i18nDescription;
    @Inject
    @DataField
    private ImageItemWidget thumbnail;
    @Inject
    @DataField
    private ValueListBox<TerrainType> physicalTerrainType;
    @Inject
    @Bound(property = "physicalAreaConfig.radius")
    @DataField
    private CommaDoubleBox physicalAreaConfigRadius;
    @Inject
    @Bound(property = "physicalAreaConfig.fixVerticalNorm")
    @DataField
    private CheckboxInput physicalAreaConfigFixVerticalNorm;
    @Inject
    @Bound(property = "physicalAreaConfig.angularVelocity", converter = GradToRadConverter.class)
    @DataField
    private CommaDoubleBox physicalAreaConfigAngularVelocity; //Rad per second
    @Inject
    @Bound(property = "physicalAreaConfig.speed")
    @DataField
    private CommaDoubleBox physicalAreaConfigSpeed;
    @Inject
    @Bound(property = "physicalAreaConfig.acceleration")
    @DataField
    private CommaDoubleBox physicalAreaConfigAcceleration;
    @Inject
    @Bound
    @DataField
    private NumberInput health;
    @Inject
    @Bound
    @DataField
    private NumberInput price;
    @Inject
    @Bound
    @DataField
    private NumberInput buildup;
    @Inject
    @Bound
    @DataField
    private NumberInput xpOnKilling;
    @Inject
    @Bound
    @DataField
    private NumberInput consumingHouseSpace;
    @Inject
    @DataField
    private BoxItemTypeWidget dropBoxItemTypeWidget;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox dropBoxPossibility;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox boxPickupRange;
    @Inject
    @Bound
    @DataField
    private NumberInput unlockCrystals;
    @Inject
    @Bound
    @DataField
    private NumberInput spawnDurationMillis;
    @Inject
    @DataField
    private AudioWidget spawnAudioId;
    @Inject
    @Bound
    @DataField
    private NumberInput explosionParticleConfigId;
    @Inject
    @DataField
    private ChildTable<DemolitionStepEffect> demolitionStepEffects;
    @Inject
    @DataField
    private Shape3DReferenceFiled wreckageShape3DId;
    @Inject
    @DataField
    private ImageItemWidget demolitionImageId;
    @Inject
    @DataField
    private ImageItemWidget buildupTextureId;
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
    @Inject
    @DataField
    private ChildContainer<ItemContainerType> itemContainerTypePanelChildTable;
    @Inject
    @DataField
    private ChildContainer<SpecialType> specialTypeChildContainer;

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
        i18nName.init(baseItemType.getI18nName(), baseItemType::setI18nName);
        i18nDescription.init(baseItemType.getI18nDescription(), baseItemType::setI18nDescription);
        dropBoxItemTypeWidget.init(baseItemType.getDropBoxItemTypeId(), baseItemType::setDropBoxItemTypeId);
        thumbnail.setImageId(baseItemType.getThumbnail(), baseItemType::setThumbnail);
        physicalTerrainType.setAcceptableValues(Arrays.asList(TerrainType.values()));
        physicalTerrainType.setValue(baseItemType.getPhysicalAreaConfig().getTerrainType());
        physicalTerrainType.addValueChangeHandler(event -> {
            baseItemType.getPhysicalAreaConfig().setTerrainType(physicalTerrainType.getValue());
        });
        spawnAudioId.init(baseItemType.getSpawnAudioId(), baseItemType::setSpawnAudioId);
        demolitionStepEffects.init(baseItemType.getDemolitionStepEffects(), baseItemType::setDemolitionStepEffects, DemolitionStepEffect::new, DemolitionStepEffectPanel.class);
        wreckageShape3DId.init(baseItemType.getWreckageShape3DId(), baseItemType::setWreckageShape3DId);
        demolitionImageId.setImageId(baseItemType.getDemolitionImageId(), baseItemType::setDemolitionImageId);
        buildupTextureId.setImageId(baseItemType.getBuildupTextureId(), baseItemType::setBuildupTextureId);
        builderTypeChildContainer.init(baseItemType.getBuilderType(), baseItemType::setBuilderType, BuilderType::new, BuilderTypePanel.class);
        factoryTypeChildContainer.init(baseItemType.getFactoryType(), baseItemType::setFactoryType, FactoryType::new, FactoryTypePanel.class);
        harvesterTypeChildContainer.init(baseItemType.getHarvesterType(), baseItemType::setHarvesterType, HarvesterType::new, HarvesterTypePanel.class);
        weaponTypeChildContainer.init(baseItemType.getWeaponType(), baseItemType::setWeaponType, WeaponType::new, WeaponTypePanel.class);


        itemContainerTypePanelChildTable.init(baseItemType.getItemContainerType(), baseItemType::setItemContainerType, ItemContainerType::new, ItemContainerTypePanel.class);


        generatorTypeChildContainer.init(baseItemType.getGeneratorType(), baseItemType::setGeneratorType, GeneratorType::new, GeneratorTypePanel.class);
        consumerTypePanelChildTable.init(baseItemType.getConsumerType(), baseItemType::setConsumerType, ConsumerType::new, ConsumerTypePanel.class);
        specialTypeChildContainer.init(baseItemType.getSpecialType(), baseItemType::setSpecialType, SpecialType::new, SpecialTypePanel.class);
    }

    @Override
    public BaseItemType getConfigObject() {
        return baseItemTypeDataBinder.getModel();
    }
}
