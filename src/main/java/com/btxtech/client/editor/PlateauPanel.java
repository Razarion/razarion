package com.btxtech.client.editor;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.PlateauConfigEntity;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import elemental.client.Browser;
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
@Templated("PlateauPanel.html#plateau")
public class PlateauPanel extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @AutoBound
    private DataBinder<PlateauConfigEntity> plateauConfigEntityDataBinder/* = DataBinder.forModel(terrainSurface.getPlateau().getPlateauConfigEntity())*/;
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
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();
    @Inject
    private SlopeEditor slopeEditor;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractal;
    @Inject
    @DataField
    private Button save;
    // private Logger logger = Logger.getLogger(PlateauPanel.class.getName());

    @PostConstruct
    public void init() {
        // plateauConfigEntityDataBinder = DataBinder.forModel(terrainSurface.getPlateau().getPlateauConfigEntity(), InitialState.FROM_MODEL);
        // slopeEditor.init(svgElement);
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        // terrainSurface.savePlateauConfigEntity();
    }
}
