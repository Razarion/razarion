package com.btxtech.client.menu;

import com.btxtech.client.TerrainEditorService;
import com.btxtech.client.VertexListService;
import com.btxtech.client.editor.EditorDialogBox;
import com.btxtech.client.terrain.TerrainSurface;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("PlateauMenu.html#plateau-terrain")
public class PlateauMenu extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @DataField
    private DoubleBox slopeTopThreshold;
    @Inject
    @DataField
    private DoubleBox slopeTopThresholdFading;
    @Inject
    @DataField
    private DoubleBox bumpMapDepth;
    @Inject
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @DataField
    private DoubleBox specularHardness;
    @Inject
    @DataField
    private Button slopeEditor;
    @Inject
    @DataField
    private Button save;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;

    // private Logger logger = Logger.getLogger(PlateauMenu.class.getName());
    private EditorDialogBox editorDialogBox;

    @PostConstruct
    public void init() {
        slopeTopThreshold.setValue(terrainSurface.getPlateau().getSlopeTopThreshold());
        slopeTopThresholdFading.setValue(terrainSurface.getPlateau().getSlopeTopThresholdFading());
        bumpMapDepth.setValue(terrainSurface.getPlateau().getBumpMapDepth());
        specularIntensity.setValue(terrainSurface.getPlateau().getSpecularIntensity());
        specularHardness.setValue(terrainSurface.getPlateau().getSpecularHardness());
    }

    @EventHandler("bumpMapDepth")
    public void bumpMapDepthChanged(ChangeEvent e) {
        terrainSurface.getPlateau().setBumpMapDepth(bumpMapDepth.getValue());
    }

    @EventHandler("specularIntensity")
    public void specularIntensityChanged(ChangeEvent e) {
        terrainSurface.getPlateau().setSpecularIntensity(specularIntensity.getValue());
    }

    @EventHandler("specularHardness")
    public void specularHardnessChanged(ChangeEvent e) {
        terrainSurface.getPlateau().setSpecularHardness(specularHardness.getValue());
    }

    @EventHandler("slopeTopThreshold")
    public void slopeTopThresholdChanged(ChangeEvent e) {
        terrainSurface.getPlateau().setSlopeTopThreshold(slopeTopThreshold.getValue());
    }

    @EventHandler("slopeTopThresholdFading")
    public void slopeTopThresholdFadingChanged(ChangeEvent e) {
        terrainSurface.getPlateau().setSlopeTopThresholdFading(slopeTopThresholdFading.getValue());
    }

    @EventHandler("slopeEditor")
    private void slopeEditorButtonClick(ClickEvent event) {
        editorDialogBox.open();
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        terrainEditorService.call().save();
    }

    public void setEditorDialogBox(EditorDialogBox editorDialogBox) {
        this.editorDialogBox = editorDialogBox;
    }
}
