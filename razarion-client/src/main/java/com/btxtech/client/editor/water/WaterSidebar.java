package com.btxtech.client.editor.water;

import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.client.editor.widgets.SpecularLightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.03.2017.
 */
@Templated("WaterSidebar.html#water")
public class WaterSidebar extends AbstractEditor {
    @Inject
    private Caller<TerrainElementEditorProvider> terrainElementEditorProvider;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    @AutoBound
    private DataBinder<WaterConfig> waterDataBinder;
    @Inject
    @DataField
    private SpecularLightWidget specularLightConfig;
    @Inject
    @Bound
    @DataField
    private NumberInput transparency;
    @Inject
    @DataField
    private ImageItemWidget reflectionId;
    @Inject
    @Bound
    @DataField
    private NumberInput reflectionScale;
    @Inject
    @DataField
    private ImageItemWidget normMapId;
    @Inject
    @Bound
    @DataField
    private NumberInput normMapDepth;
    @Inject
    @DataField
    private ImageItemWidget distortionId;
    @Inject
    @Bound
    @DataField
    private NumberInput distortionScale;
    @Inject
    @Bound
    @DataField
    private NumberInput distortionStrength;
    @Inject
    @Bound
    @DataField
    private NumberInput distortionDurationSeconds;
    @Inject
    @Bound
    @DataField
    private NumberInput waterLevel;
    @Inject
    @Bound
    @DataField
    private NumberInput groundLevel;

    @PostConstruct
    public void init() {
        waterDataBinder.setModel(terrainTypeService.getWaterConfig());
        specularLightConfig.setModel(terrainTypeService.getWaterConfig().getSpecularLightConfig());
        // TODO terrainUiService.enableEditMode(visualUiService.getStaticVisualConfig().getWaterConfig());
        reflectionId.setImageId(terrainTypeService.getWaterConfig().getReflectionId(), imageId -> {
            terrainTypeService.getWaterConfig().setReflectionId(imageId);
            terrainUiService.onEditorTerrainChanged();
        });
        normMapId.setImageId(terrainTypeService.getWaterConfig().getNormMapId(), imageId -> {
            terrainTypeService.getWaterConfig().setNormMapId(imageId);
            terrainUiService.onEditorTerrainChanged();
        });
        distortionId.setImageId(terrainTypeService.getWaterConfig().getDistortionId(), imageId -> {
            terrainTypeService.getWaterConfig().setDistortionId(imageId);
            terrainUiService.onEditorTerrainChanged();
        });
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> {
            terrainElementEditorProvider.call(response -> {
            }, exceptionHandler.restErrorHandler("saveWaterConfig failed: ")).saveWaterConfig(terrainTypeService.getWaterConfig());
        });
        enableSaveButton(true);
    }

}
