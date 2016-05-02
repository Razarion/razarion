package com.btxtech.client.menu;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.GroundConfigEntity;
import com.btxtech.shared.GroundSkeletonEntity;
import com.btxtech.shared.SlopeConfigEntity;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
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
@Templated("TerrainMenu.html#menu-terrain")
public class TerrainMenu extends Composite {
    // private Logger logger = Logger.getLogger(TerrainMenu.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private RenderService renderService;
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
    private Button saveButton;

    @PostConstruct
    public void init() {
        groundConfigEntityDataBinder.setModel(terrainSurface.getGroundConfigEntity());
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        terrainSurface.fillBuffers();
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        // terrainSurface.saveTerrain();
    }
}
