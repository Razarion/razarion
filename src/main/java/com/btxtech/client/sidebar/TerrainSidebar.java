package com.btxtech.client.sidebar;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.GroundSkeletonFactory;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.GroundConfigEntity;
import com.btxtech.shared.GroundSkeletonEntity;
import com.btxtech.shared.TerrainEditorService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
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
public class TerrainSidebar extends Composite {
    private Logger logger = Logger.getLogger(TerrainSidebar.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private RenderService renderService;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    @AutoBound
    private DataBinder<GroundConfigEntity> groundConfigEntityDataBinder;
    @Inject
    @Bound
    @DataField
    private DoubleBox splattingDistance;
    @Inject
    @Bound
    @DataField
    private IntegerBox splattingXCount;
    @Inject
    @Bound
    @DataField
    private IntegerBox splattingYCount;
    @Inject
    @Bound
    @DataField
    private DoubleBox splattingFractalMin;
    @Inject
    @Bound
    @DataField
    private DoubleBox splattingFractalMax;
    @Inject
    @Bound
    @DataField
    private DoubleBox splattingFractalRoughness;
    @Inject
    @Bound
    @DataField
    private DoubleBox bumpMapDepth;
    @Inject
    @Bound
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @Bound
    @DataField
    private DoubleBox specularHardness;
    @Inject
    @Bound
    @DataField
    private IntegerBox heightXCount;
    @Inject
    @Bound
    @DataField
    private IntegerBox heightYCount;
    @Inject
    @Bound
    @DataField
    private DoubleBox heightFractalShift;
    @Inject
    @Bound
    @DataField
    private DoubleBox heightFractalRoughness;
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
        terrainEditorService.call(new RemoteCallback<GroundConfigEntity>() {
            @Override
            public void callback(GroundConfigEntity groundConfigEntity) {
                groundConfigEntityDataBinder.setModel(groundConfigEntity);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadGroundConfig failed: " + message, throwable);
                return false;
            }
        }).loadGroundConfig();
    }

    @EventHandler("updateButton")
    private void updateButtonClick(ClickEvent event) {
        GroundConfigEntity groundConfigEntity = groundConfigEntityDataBinder.getModel();
        GroundSkeletonEntity groundSkeletonEntity = groundConfigEntity.getGroundSkeletonEntity();
        groundSkeletonEntity.setValues(groundConfigEntity);
        terrainSurface.setGroundSkeletonEntity(groundSkeletonEntity);
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        GroundConfigEntity groundConfigEntity = groundConfigEntityDataBinder.getModel();
        GroundSkeletonFactory.sculpt(groundConfigEntity);
        terrainSurface.setGroundSkeletonEntity(groundConfigEntity.getGroundSkeletonEntity());
        terrainSurface.fillBuffers();
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        terrainEditorService.call(new RemoteCallback<GroundConfigEntity>() {
            @Override
            public void callback(GroundConfigEntity groundConfigEntity) {
                groundConfigEntityDataBinder.setModel(groundConfigEntity);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveGroundConfig failed: " + message, throwable);
                return false;
            }
        }).saveGroundConfig(groundConfigEntityDataBinder.getModel());
    }
}
