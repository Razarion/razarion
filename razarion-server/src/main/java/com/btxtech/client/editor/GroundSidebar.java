package com.btxtech.client.editor;

import com.btxtech.client.dialog.ApplyListener;
import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.editor.fractal.FractalDialog;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.shared.TerrainElementEditorProvider;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
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
public class GroundSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(GroundSidebar.class.getName());
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private Caller<TerrainElementEditorProvider> terrainEditorService;
    @Inject
    @AutoBound
    private DataBinder<GroundConfig> groundConfigDataBinder;
    @Inject
    @DataField
    private LightWidget lightConfig;
    @Inject
    @DataField
    private Button fractalSplatting;
    @Inject
    @DataField
    private Button fractalHeight;
    @Inject
    @Bound(property = "groundSkeletonConfig.topBmDepth")
    @DataField
    private DoubleBox topBmDepth;
    @Inject
    @Bound(property = "groundSkeletonConfig.bottomBmDepth")
    @DataField
    private DoubleBox bottomBmDepth;
    @Inject
    @DataField
    private Button sculptButton;
    @Inject
    @DataField
    private Button updateButton;
    @Inject
    @DataField
    private Button saveButton;

    @PostConstruct
    public void init() {
        terrainEditorService.call(new RemoteCallback<GroundConfig>() {
            @Override
            public void callback(GroundConfig groundConfig) {
                groundConfigDataBinder.setModel(groundConfig);
                lightConfig.setModel(groundConfig.getGroundSkeletonConfig().getLightConfig());
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadGroundConfig failed: " + message, throwable);
                return false;
            }
        }).loadGroundConfig();
    }

    @Override
    public void onClose() {
        // Ignore
    }

    @EventHandler("updateButton")
    private void updateButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        // TODO terrainUiService.setGroundSkeleton(groundConfig.getGroundSkeletonConfig());
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        // TODO terrainUiService.setGroundSkeleton(groundConfig.getGroundSkeletonConfig());
        // TODO terrainUiService.setup();
        renderService.fillBuffers();
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        terrainEditorService.call(new RemoteCallback<GroundConfig>() {
            @Override
            public void callback(GroundConfig groundConfig) {
                groundConfigDataBinder.setModel(groundConfig);
                lightConfig.setModel(groundConfig.getGroundSkeletonConfig().getLightConfig());
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveGroundConfig failed: " + message, throwable);
                return false;
            }
        }).saveGroundConfig(groundConfigDataBinder.getModel());
    }

    @EventHandler("fractalSplatting")
    private void fractalSplattingButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFieldConfig fractalFieldConfig = groundConfig.toSplattingFractalFiledConfig();
        modalDialogManager.show("Splatting Fractal Dialog", FractalDialog.class, fractalFieldConfig, new ApplyListener<FractalFieldConfig>() {
            @Override
            public void onApply(FractalFieldConfig fractalFieldConfig1) {
                GroundConfig groundConfig = groundConfigDataBinder.getModel();
                groundConfig.fromSplattingFractalFiledConfig(fractalFieldConfig);
            }
        });
    }

    @EventHandler("fractalHeight")
    private void fractalHeightButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFieldConfig fractalFieldConfig = groundConfig.toHeightFractalFiledConfig();
        modalDialogManager.show("Height Fractal Dialog", FractalDialog.class, fractalFieldConfig, new ApplyListener<FractalFieldConfig>() {
            @Override
            public void onApply(FractalFieldConfig fractalFieldConfig1) {
                GroundConfig groundConfig = groundConfigDataBinder.getModel();
                groundConfig.fromHeightFractalFiledConfig(fractalFieldConfig);
            }
        });
    }

}
