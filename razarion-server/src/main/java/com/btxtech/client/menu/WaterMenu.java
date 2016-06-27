package com.btxtech.client.menu;

import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.uiservice.terrain.Water;
import com.btxtech.client.widgets.LightWidget;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("WaterMenu.html#menu-water")
public class WaterMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @DataField
    private LightWidget lightWidget;
    @Inject
    @DataField
    private DoubleBox transparency;
    @Inject
    @DataField
    private DoubleBox bumpMap;
    @Inject
    @DataField
    private DoubleBox level;
    @Inject
    @DataField
    private DoubleBox ground;

    @PostConstruct
    public void init() {
        Water water = terrainSurface.getWater();
        transparency.setValue(water.getWaterTransparency());
        bumpMap.setValue(water.getWaterBumpMapDepth());
        level.setValue(water.getLevel());
        ground.setValue(water.getGround());
        lightWidget.setModel(water.getLightConfig());
    }

    @EventHandler("transparency")
    public void transparencyChanged(ChangeEvent e) {
        terrainSurface.getWater().setWaterTransparency(transparency.getValue());
    }

    @EventHandler("bumpMap")
    public void bumpMapChanged(ChangeEvent e) {
        terrainSurface.getWater().setWaterBumpMapDepth(bumpMap.getValue());
    }

    @EventHandler("level")
    public void levelChanged(ChangeEvent e) {
        terrainSurface.getWater().setLevel(level.getValue());
    }

    @EventHandler("ground")
    public void groundChanged(ChangeEvent e) {
        terrainSurface.getWater().setGround(ground.getValue());
    }
}
