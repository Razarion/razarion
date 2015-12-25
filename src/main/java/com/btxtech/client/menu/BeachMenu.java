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
@Templated("BeachMenu.html#menu-beach")
public class BeachMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @DataField
    private DoubleBox bumpMap;
    @Inject
    @DataField
    private DoubleBox fractal;
//    @Inject
//    @DataField
//    private DoubleBox specularIntensity;
//    @Inject
//    @DataField
//    private DoubleBox specularHardness;

    @PostConstruct
    public void init() {
        bumpMap.setValue(terrainSurface.getBeach().getBumpMap());
        fractal.setValue(terrainSurface.getBeach().getFractal());
        // specularIntensity.setValue(terrainSurface.getBeach().getWaterSpecularIntensity());
        // specularHardness.setValue(terrainSurface.getBeach().getWaterSpecularHardness());
    }

    @EventHandler("bumpMap")
    public void bumpMapChanged(ChangeEvent e) {
        terrainSurface.getBeach().setBumpMap(bumpMap.getValue());
    }

    @EventHandler("fractal")
    public void fractalChanged(ChangeEvent e) {
        terrainSurface.getBeach().setFractal(fractal.getValue());
    }

//    @EventHandler("specularIntensity")
//    public void specularIntensityChanged(ChangeEvent e) {
//        terrainSurface.getBeach().setWaterSpecularIntensity(specularIntensity.getValue());
//    }
//
//    @EventHandler("specularHardness")
//    public void specularHardnessChanged(ChangeEvent e) {
//        terrainSurface.getBeach().setWaterSpecularHardness(specularHardness.getValue());
//    }
}
