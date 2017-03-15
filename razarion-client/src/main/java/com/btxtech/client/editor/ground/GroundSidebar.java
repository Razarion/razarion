package com.btxtech.client.editor.ground;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.fractal.FractalDialog;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
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
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private Caller<TerrainElementEditorProvider> terrainEditorService;
    @Inject
    private GroundRenderTask groundRenderTask;
    @Inject
    @AutoBound
    private DataBinder<GroundConfig> groundConfigDataBinder;
    @Inject
    @DataField
    private LightWidget lightConfig;
    @Inject
    @DataField
    private ImageItemWidget topTextureId;
    @Bound(property = "groundSkeletonConfig.topTextureScale")
    @Inject
    @DataField
    private NumberInput topTextureScale;
    @Inject
    @DataField
    private ImageItemWidget topBmId;
    @Bound(property = "groundSkeletonConfig.topBmScale")
    @Inject
    @DataField
    private NumberInput topBmScale;
    @Inject
    @Bound(property = "groundSkeletonConfig.topBmDepth")
    @DataField
    private NumberInput topBmDepth;
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
    @Inject
    @DataField
    private Button fractalSplatting;
    @Inject
    @DataField
    private Button fractalHeight;
    @Inject
    @DataField
    private Button sculptButton;

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
                terrainUiService.enableEditMode(groundConfig.getGroundSkeletonConfig());
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
                terrainUiService.enableEditMode(groundConfig.getGroundSkeletonConfig());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "saveGroundConfig failed: " + message, throwable);
            return false;
        }).saveGroundConfig(groundConfigDataBinder.getModel()));
        enableSaveButton(true);
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
        modalDialogManager.show("Splatting Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, (button, fractalFieldConfig1) -> {
            if (button == DialogButton.Button.APPLY) {
                GroundConfig groundConfig1 = groundConfigDataBinder.getModel();
                groundConfig1.fromSplattingFractalFiledConfig(fractalFieldConfig);
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    @EventHandler("fractalHeight")
    private void fractalHeightButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFieldConfig fractalFieldConfig = groundConfig.toHeightFractalFiledConfig();
        modalDialogManager.show("Height Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, (button, fractalFieldConfig1) -> {
            if (button == DialogButton.Button.APPLY) {
                GroundConfig groundConfig1 = groundConfigDataBinder.getModel();
                groundConfig1.fromHeightFractalFiledConfig(fractalFieldConfig);
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

}
