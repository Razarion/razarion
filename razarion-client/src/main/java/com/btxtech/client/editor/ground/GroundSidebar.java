package com.btxtech.client.editor.ground;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.fractal.FractalDialog;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.SpecularLightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("GroundSidebar.html#terrain")
public class GroundSidebar extends LeftSideBarContent {
    //private Logger logger = Logger.getLogger(GroundSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private Caller<TerrainElementEditorProvider> terrainEditorService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<PlanetEditorProvider> planetEditorServiceCaller;
    @Inject
    @AutoBound
    private DataBinder<GroundConfig> groundConfigDataBinder;
    @Inject
    @DataField
    private SpecularLightWidget specularLightConfig;
    @Inject
    @DataField
    private ImageItemWidget topTextureId;
    @Bound(property = "groundSkeletonConfig.topTextureScale")
    @Inject
    @DataField
    private NumberInput topTextureScale;
    @Inject
    @DataField
    private ImageItemWidget bottomTextureId;
    @Bound(property = "groundSkeletonConfig.bottomTextureScale")
    @Inject
    @DataField
    private NumberInput bottomTextureScale;
    @Inject
    @DataField
    private ImageItemWidget bottomBmId;
    @Bound(property = "groundSkeletonConfig.bottomBmScale")
    @Inject
    @DataField
    private NumberInput bottomBmScale;
    @Inject
    @Bound(property = "groundSkeletonConfig.bottomBmDepth")
    @DataField
    private NumberInput bottomBmDepth;
    @Inject
    @DataField
    private ImageItemWidget splattingId;
    @Bound(property = "groundSkeletonConfig.splattingScale")
    @Inject
    @DataField
    private NumberInput splattingScale;
    @Bound(property = "groundSkeletonConfig.splattingFadeThreshold")
    @Inject
    @DataField
    private NumberInput splattingFadeThreshold;
    @Bound(property = "groundSkeletonConfig.splattingOffset")
    @Inject
    @DataField
    private NumberInput splattingOffset;
    @Bound(property = "groundSkeletonConfig.splattingGroundBmMultiplicator")
    @Inject
    @DataField
    private NumberInput splattingGroundBmMultiplicator;
    @Inject
    @DataField
    private Button fractalSplatting;
    @Inject
    @DataField
    private Button fractalHeight;
    @Inject
    @DataField
    private Button restartPlanetButton;

    @PostConstruct
    public void init() {
        terrainEditorService.call((RemoteCallback<GroundConfig>) groundConfig -> {
            groundConfigDataBinder.setModel(groundConfig);
            specularLightConfig.setModel(groundConfig.getGroundSkeletonConfig().getSpecularLightConfig());
//   TODO         topTextureId.setImageId(groundConfig.getGroundSkeletonConfig().getTopTextureId(), imageId -> {
//                groundConfig.getGroundSkeletonConfig().setTopTextureId(imageId);
//                terrainUiService.onEditorTerrainChanged();
//            });
//            bottomTextureId.setImageId(groundConfig.getGroundSkeletonConfig().getBottomTextureId(), imageId -> {
//                groundConfig.getGroundSkeletonConfig().setBottomTextureId(imageId);
//                terrainUiService.onEditorTerrainChanged();
//            });
//            bottomBmId.setImageId(groundConfig.getGroundSkeletonConfig().getBottomBmId(), imageId -> {
//                groundConfig.getGroundSkeletonConfig().setBottomBmId(imageId);
//                terrainUiService.onEditorTerrainChanged();
//            });
//            splattingId.setImageId(groundConfig.getGroundSkeletonConfig().getSplattingId(), imageId -> {
//                groundConfig.getGroundSkeletonConfig().setSplattingId(imageId);
//                terrainUiService.onEditorTerrainChanged();
//            });
//            terrainUiService.enableEditMode(groundConfig.getGroundSkeletonConfig());
        }, exceptionHandler.restErrorHandler("loadGroundConfig failed: ")).loadGroundConfig();
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> terrainEditorService.call((RemoteCallback<GroundConfig>) groundConfig -> {
            groundConfigDataBinder.setModel(groundConfig);
            specularLightConfig.setModel(groundConfig.getGroundSkeletonConfig().getSpecularLightConfig());
            // TODO terrainUiService.enableEditMode(groundConfig.getGroundSkeletonConfig());
        }, exceptionHandler.restErrorHandler("saveGroundConfig failed: ")).saveGroundConfig(groundConfigDataBinder.getModel()));
        enableSaveButton(true);
    }

    @EventHandler("restartPlanetButton")
    private void restartPlanetButtonClicked(ClickEvent event) {
        modalDialogManager.showQuestionDialog("Restart planet", "Really restart the planet? Close all current connections.", () -> planetEditorServiceCaller.call(ignore -> {
        }, exceptionHandler.restErrorHandler("PlanetEditorProvider.restartPlanetWarm() failed: ")).restartPlanetCold(gameUiControl.getPlanetConfig().getPlanetId()), () -> {
        });
    }

    @EventHandler("fractalSplatting")
    private void fractalSplattingButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        FractalFieldConfig fractalFieldConfig = groundConfig.toSplattingFractalFiledConfig();
//    TODO    modalDialogManager.show("Splatting Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, (button, fractalFieldConfig1) -> {
// TODO           if (button == DialogButton.Button.APPLY) {
// TODO               GroundConfig groundConfig1 = groundConfigDataBinder.getModel();
// TODO               groundConfig1.fromSplattingFractalFiledConfig(fractalFieldConfig);
// TODO           }
// TODO       }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    @EventHandler("fractalHeight")
    private void fractalHeightButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFieldConfig fractalFieldConfig = groundConfig.toHeightFractalFiledConfig();
// TODO       modalDialogManager.show("Height Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, (button, fractalFieldConfig1) -> {
// TODO           if (button == DialogButton.Button.APPLY) {
// TODO               GroundConfig groundConfig1 = groundConfigDataBinder.getModel();
// TODO               groundConfig1.fromHeightFractalFiledConfig(fractalFieldConfig);
// TODO           }
// TODO       }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

}
