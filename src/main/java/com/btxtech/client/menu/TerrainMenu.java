package com.btxtech.client.menu;

import com.btxtech.client.terrain.TerrainSurface;
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
@Templated("TerrainMenu.html#menu-terrain")
public class TerrainMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @DataField("surfaceSlider")
    private DoubleBox surfaceSlider;
    // private Logger logger = Logger.getLogger(TerrainMenu.class.getName());

    @PostConstruct
    public void init() {
        surfaceSlider.setValue(terrainSurface.getEdgeDistance());
    }

    @EventHandler("surfaceSlider")
    public void surfaceSliderChanged(ChangeEvent e) {
        terrainSurface.setEdgeDistance(surfaceSlider.getValue());
    }

}
