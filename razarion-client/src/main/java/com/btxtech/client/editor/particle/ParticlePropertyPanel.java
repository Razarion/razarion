package com.btxtech.client.editor.particle;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.guielements.VertexBox;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.particle.AutonomousParticleEmitterConfig;
import com.btxtech.uiservice.particle.DependentParticleEmitterConfig;
import com.btxtech.uiservice.particle.ParticleEmitterSequenceConfig;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("ParticlePropertyPanel.html#particle-property-panel")
public class ParticlePropertyPanel extends AbstractPropertyPanel<ParticleEmitterSequenceConfig> {
    // private Logger logger = Logger.getLogger(ClipPropertyPanel.class.getName());
    @Inject
    @AutoBound
    private DataBinder<ParticleEmitterSequenceConfig> dataBinder;
    @Inject
    private ParticleService particleService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private TerrainUiService terrainUiService;
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
    @ListContainer("tbody")
    private ListComponent<DependentParticleEmitterConfig, DependentParticleEmitterConfigWidget> dependent;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<AutonomousParticleEmitterConfig, AutonomousParticleEmitterConfigWidget> autonomous;
    @Inject
    @DataField
    private VertexBox testPosition;
    @Inject
    @DataField
    private VertexBox testDirection;
    @Inject
    @DataField
    private Button testParticleButton;
    @Inject
    @DataField
    private Button clearParticleButton;

    @Override
    public void init(ParticleEmitterSequenceConfig particleEmitterSequenceConfig) {
        DOMUtil.removeAllElementChildren(dependent.getElement()); // Remove placeholder table row from template.
        DOMUtil.removeAllElementChildren(autonomous.getElement()); // Remove placeholder table row from template.
        dataBinder.setModel(particleEmitterSequenceConfig);
        onChange(particleEmitterSequenceConfig);
        testPosition.setValue(terrainUiService.getPosition3d(terrainScrollHandler.getCurrentViewField().calculateCenter()));
    }

    @Override
    public ParticleEmitterSequenceConfig getConfigObject() {
        return dataBinder.getModel();
    }

    @EventHandler("testParticleButton")
    private void testParticleButtonClick(ClickEvent event) {
        try {
            Vertex position = testPosition.getValue();
            if (position != null) {
                particleService.start(System.currentTimeMillis(), position, testDirection.getValue(), getConfigObject().getId());
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("clearParticleButton")
    private void clearParticleButtonClick(ClickEvent event) {
        particleService.clear();
    }

}
