package com.btxtech.client.menu;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainSurface;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
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
@Templated("TerrainMenu.html#menu-terrain")
public class TerrainMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private RenderService renderService;
    @Inject
    @DataField("surfaceSlider")
    private DoubleBox surfaceSlider;
    @Inject
    @DataField("groundBumpMap")
    private DoubleBox groundBumpMap;
    @Inject
    @DataField
    private Button sculptButton;
    @Inject
    @DataField
    private Button saveButton;


    // private Logger logger = Logger.getLogger(TerrainMenu.class.getName());

    @PostConstruct
    public void init() {
        surfaceSlider.setValue(terrainSurface.getSplattingBlur());
        groundBumpMap.setValue(terrainSurface.getGroundBumpMap());
    }

    @EventHandler("surfaceSlider")
    public void surfaceSliderChanged(ChangeEvent e) {
        terrainSurface.setEdgeDistance(surfaceSlider.getValue());
    }

    @EventHandler("groundBumpMap")
    public void groundBumpMapChanged(ChangeEvent e) {
        terrainSurface.setGroundBumpMap(groundBumpMap.getValue());
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        terrainSurface.sculpt();
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        // terrainSurface.saveTerrain();
    }
}
