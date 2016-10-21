package com.btxtech.client.editor.ground;

import com.btxtech.client.dialog.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.fractal.FractalDialog;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("GroundSidebar.html#terrain")
public class GroundSidebar extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(GroundSidebar.class.getName());
    @Inject
    private TerrainService terrainService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ClientRenderServiceImpl renderService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<TerrainElementEditorProvider> terrainEditorService;
    @Inject
    private GroundRenderTask groundRenderTask;
    @Inject
    @AutoBound
    private DataBinder<GroundConfig> groundConfigDataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private LightWidget lightConfig;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget topTextureId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound(property = "groundSkeletonConfig.topTextureScale")
    @Inject
    @DataField
    private DoubleBox topTextureScale;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget topBmId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound(property = "groundSkeletonConfig.topBmScale")
    @Inject
    @DataField
    private DoubleBox topBmScale;
    @Inject
    @Bound(property = "groundSkeletonConfig.topBmDepth")
    @DataField
    private DoubleBox topBmDepth;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget bottomTextureId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound(property = "groundSkeletonConfig.bottomTextureScale")
    @Inject
    @DataField
    private DoubleBox bottomTextureScale;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget bottomBmId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound(property = "groundSkeletonConfig.bottomBmScale")
    @Inject
    @DataField
    private DoubleBox bottomBmScale;
    @Inject
    @Bound(property = "groundSkeletonConfig.bottomBmDepth")
    @DataField
    private DoubleBox bottomBmDepth;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ImageItemWidget splattingId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Bound(property = "groundSkeletonConfig.splattingScale")
    @Inject
    @DataField
    private DoubleBox splattingScale;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button fractalSplatting;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button fractalHeight;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button sculptButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button updateButton;

    @PostConstruct
    public void init() {
        terrainEditorService.call(new RemoteCallback<GroundConfig>() {
            @Override
            public void callback(GroundConfig groundConfig) {
                groundConfigDataBinder.setModel(groundConfig);
                lightConfig.setModel(groundConfig.getGroundSkeletonConfig().getLightConfig());
                topTextureId.setImageId(groundConfig.getGroundSkeletonConfig().getTopTextureId(), imageId -> groundConfig.getGroundSkeletonConfig().setTopTextureId(imageId));
                topBmId.setImageId(groundConfig.getGroundSkeletonConfig().getTopBmId(), imageId -> groundConfig.getGroundSkeletonConfig().setTopBmId(imageId));
                bottomTextureId.setImageId(groundConfig.getGroundSkeletonConfig().getBottomTextureId(), imageId -> groundConfig.getGroundSkeletonConfig().setBottomTextureId(imageId));
                bottomBmId.setImageId(groundConfig.getGroundSkeletonConfig().getBottomBmId(), imageId -> groundConfig.getGroundSkeletonConfig().setBottomBmId(imageId));
                splattingId.setImageId(groundConfig.getGroundSkeletonConfig().getSplattingId(), imageId -> groundConfig.getGroundSkeletonConfig().setSplattingId(imageId));
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "loadGroundConfig failed: " + message, throwable);
            return false;
        }).loadGroundConfig();
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> terrainEditorService.call(new RemoteCallback<GroundConfig>() {
            @Override
            public void callback(GroundConfig groundConfig) {
                groundConfigDataBinder.setModel(groundConfig);
                lightConfig.setModel(groundConfig.getGroundSkeletonConfig().getLightConfig());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "saveGroundConfig failed: " + message, throwable);
            return false;
        }).saveGroundConfig(groundConfigDataBinder.getModel()));
        enableSaveButton(true);
    }

    @EventHandler("updateButton")
    private void updateButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        terrainTypeService.setGroundSkeletonConfig(groundConfig.getGroundSkeletonConfig());
        terrainService.setupGround();
        groundRenderTask.onChanged();
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        // TODO GroundConfig groundConfig = groundConfigDataBinder.getModel();
        // TODO terrainUiService.setGroundSkeleton(groundConfig.getGroundSkeletonConfig());
        // TODO terrainUiService.setup();
        groundRenderTask.onChanged();
    }

    @EventHandler("fractalSplatting")
    private void fractalSplattingButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFieldConfig fractalFieldConfig = groundConfig.toSplattingFractalFiledConfig();
        modalDialogManager.show("Splatting Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, fractalFieldConfig1 -> {
            GroundConfig groundConfig1 = groundConfigDataBinder.getModel();
            groundConfig1.fromSplattingFractalFiledConfig(fractalFieldConfig);
        });
    }

    @EventHandler("fractalHeight")
    private void fractalHeightButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFieldConfig fractalFieldConfig = groundConfig.toHeightFractalFiledConfig();
        modalDialogManager.show("Height Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, fractalFieldConfig1 -> {
            GroundConfig groundConfig1 = groundConfigDataBinder.getModel();
            groundConfig1.fromHeightFractalFiledConfig(fractalFieldConfig);
        });
    }

}
