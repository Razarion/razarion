package com.btxtech.client.slopeeditor;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.TerrainEditorService;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import elemental.client.Browser;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("SlopePanel.html#slope")
public class SlopePanel extends Composite {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    @AutoBound
    private DataBinder<SlopeConfigEntity> plateauConfigEntityDataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
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
    private IntegerBox verticalSpace;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalShift;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalRoughness;
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();
    @Inject
    @DataField
    private Button zoomIn;
    @Inject
    @DataField
    private Button zoomOut;
    @Inject
    private SlopeEditor slopeEditor;
    @Inject
    @DataField
    private Button sculpt;
    @Inject
    @DataField
    private Button save;
    @Inject
    @DataField
    private DoubleBox helperLine;
    private Logger logger = Logger.getLogger(SlopePanel.class.getName());

    public void init(SlopeConfigEntity slopeConfigEntity) {
        plateauConfigEntityDataBinder.setModel(slopeConfigEntity);
        slopeEditor.init(svgElement, slopeConfigEntity);
    }

    @EventHandler("zoomIn")
    private void zoomInButtonClick(ClickEvent event) {
        slopeEditor.zoomIn();
    }

    @EventHandler("zoomOut")
    private void zoomOutButtonClick(ClickEvent event) {
        slopeEditor.zoomOut();
    }

    @EventHandler("helperLine")
    public void groundChanged(ChangeEvent e) {
        slopeEditor.setHelperLine(helperLine.getValue());
    }

    @EventHandler("sculpt")
    private void sculptButtonClick(ClickEvent event) {
        terrainSurface.sculpt();
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        terrainEditorService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void response) {

            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }
        }).save(plateauConfigEntityDataBinder.getModel());
    }
}
