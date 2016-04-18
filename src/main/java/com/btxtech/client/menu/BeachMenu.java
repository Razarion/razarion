package com.btxtech.client.menu;

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
@Templated("BeachMenu.html#menu-beach")
public class BeachMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @AutoBound
    private DataBinder<SlopeConfigEntity> slopeConfigEntityDataBinder/* = DataBinder.forModel(terrainSurface.getPlateau().getPlateauConfigEntity())*/;
    @Inject
    @Bound
    @DataField
    private DoubleBox slopeGroundSplattingBumpDepth;
    @Inject
    @Bound
    @DataField
    private DoubleBox slopeFactorDistance;
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
    private DoubleBox fractalShift;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalRoughness;
    @Inject
    @Bound
    @DataField
    private IntegerBox verticalSpace;
    @Inject
    @DataField
    private Button sculpt;

    @PostConstruct
    public void init() {
        slopeConfigEntityDataBinder = DataBinder.forModel(terrainSurface.getBeachSlopeConfigEntity(), InitialState.FROM_MODEL);
    }

    @EventHandler("sculpt")
    private void sculptButtonClick(ClickEvent event) {
        terrainSurface.sculpt();
    }

}
