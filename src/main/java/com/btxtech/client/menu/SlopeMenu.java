package com.btxtech.client.menu;

import com.btxtech.client.slopeeditor.PanelContainer;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.SlopeConfigEntity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
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
@Templated("SlopeMenu.html#menu-slope")
public class SlopeMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @DataField
    private Button plateau;
    @Inject
    @DataField
    private Button beach;
    private PanelContainer panelContainer;

    @EventHandler("plateau")
    private void plateauButtonClick(ClickEvent event) {
        panelContainer.showSlopeEditor(0);
    }

    @EventHandler("beach")
    private void beachButtonClick(ClickEvent event) {
        panelContainer.showSlopeEditor(1);
    }

    public void setEditorPanelContainer(PanelContainer panelContainer) {
        this.panelContainer = panelContainer;
    }
}
