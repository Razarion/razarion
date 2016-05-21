package com.btxtech.client.sidebar;

import com.btxtech.client.dialog.ModalDialog;
import com.btxtech.client.dialog.content.fractal.FractalDialog;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.FractalFiledConfig;
import com.btxtech.client.terrain.GroundModeler;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.TerrainEditorService;
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
@Templated("TerrainSidebar.html#terrain")
public class TerrainSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(TerrainSidebar.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ModalDialog modalDialog;
    @Inject
    private RenderService renderService;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    @AutoBound
    private DataBinder<GroundConfig> groundConfigDataBinder;
    @Inject
    @DataField
    private Button fractalSplatting;
    @Inject
    @DataField
    private Button fractalHeight;
    @Inject
    @Bound(property = "groundSkeleton.splattingDistance")
    @DataField
    private DoubleBox splattingDistance;
    @Inject
    @Bound(property = "groundSkeleton.bumpMapDepth")
    @DataField
    private DoubleBox bumpMapDepth;
    @Inject
    @Bound(property = "groundSkeleton.specularIntensity")
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @Bound(property = "groundSkeleton.specularHardness")
    @DataField
    private DoubleBox specularHardness;
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
        terrainSurface.setGroundSkeleton(groundConfig.getGroundSkeleton());
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        terrainSurface.setGroundSkeleton(groundConfig.getGroundSkeleton());
        terrainSurface.fillBuffers();
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        terrainEditorService.call(new RemoteCallback<GroundConfig>() {
            @Override
            public void callback(GroundConfig groundConfig) {
                groundConfigDataBinder.setModel(groundConfig);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveGroundConfig failed: " + message, throwable);
                return false;
            }
        }).saveGroundConfig(groundConfigDataBinder.getModel());
    }

    // TODO fractalHeightButtonClick + slope

    @EventHandler("fractalSplatting")
    private void fractalSplattingButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFiledConfig fractalFiledConfig = groundConfig.toSplattingFractalFiledConfig();
        modalDialog.show("Splatting Fractal Dialog", FractalDialog.class, fractalFiledConfig, new Runnable() {
            @Override
            public void run() {
                GroundConfig groundConfig = groundConfigDataBinder.getModel();
                groundConfig.fromSplattingFractalFiledConfig(fractalFiledConfig);
            }
        });
    }


    @EventHandler("fractalHeight")
    private void fractalHeightButtonClick(ClickEvent event) {
        GroundConfig groundConfig = groundConfigDataBinder.getModel();
        final FractalFiledConfig fractalFiledConfig = groundConfig.toHeightFractalFiledConfig();
        modalDialog.show("Height Fractal Dialog", FractalDialog.class, fractalFiledConfig, new Runnable() {
            @Override
            public void run() {
                GroundConfig groundConfig = groundConfigDataBinder.getModel();
                groundConfig.fromHeightFractalFiledConfig(fractalFiledConfig);
            }
        });
    }

}
