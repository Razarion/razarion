package com.btxtech.client.menu;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("Menu.html#menu-template")
public class Menu extends Composite {
    @Inject
    @DataField("menu-terrain")
    private TerrainMenu terrainMenu;
    @Inject
    @DataField("menu-light")
    private LightMenu lightMenu;
    @Inject
    @DataField("menu-camera")
    private CameraMenu cameraMenu;
    @Inject
    @DataField("menu-debug")
    private DebugMenu debugMenu;

}
