package com.btxtech.client.editor;

import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.client.editor.widgets.LightDirectionWidget;
import com.btxtech.client.guielements.VertexRoBox;
import com.btxtech.client.utils.HtmlColor2ColorConverter;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.rest.TerrainEditorController;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewService;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2016.
 */
@Templated("PlanetVisualConfigPanel.html#planet-panel")
public class PlanetVisualConfigPanel extends AbstractEditor {
    private Logger logger = Logger.getLogger(PlanetVisualConfigPanel.class.getName());
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ViewService viewService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<TerrainEditorController> planetEditorProviderCaller;
    @Inject
    @AutoBound
    private DataBinder<PlanetVisualConfig> planetVisualConfigDataBinder;
    @Inject
    @Bound
    @DataField
    private LightDirectionWidget lightDirection;
    @Inject
    @Bound(property = "lightDirection")
    @DataField
    private VertexRoBox lightDirectionBox;
    @Inject
    @Bound(converter = HtmlColor2ColorConverter.class)
    @DataField
    private TextBox ambient;
    @Inject
    @Bound(converter = HtmlColor2ColorConverter.class)
    @DataField
    private TextBox diffuse;
    @Inject
    @Bound
    @DataField
    private NumberInput shadowAlpha;

    @PostConstruct
    public void init() {
        planetVisualConfigDataBinder.setModel(visualUiService.getPlanetVisualConfig());
        planetVisualConfigDataBinder.addPropertyChangeHandler(event -> {
            shadowUiService.setupMatrices();
            viewService.onViewChanged();
            enableSaveButton(true);
        });
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> {
            planetEditorProviderCaller.call(response -> {

            }, (message, throwable) -> {
                logger.log(Level.SEVERE, "getAudioItemConfigs failed: " + message, throwable);
                return false;
            }).updatePlanetVisualConfig(gameUiControl.getPlanetConfig().getId(), visualUiService.getPlanetVisualConfig());
        });
        enableSaveButton(false);
    }

}
