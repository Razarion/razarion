package com.btxtech.client.editor.particle;

import com.btxtech.client.guielements.VertexBox;
import com.btxtech.shared.datatypes.particle.AutonomousParticleEmitterConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.02.2017.
 */
@Templated("ParticlePropertyPanel.html#autonomousTr")
public class AutonomousParticleEmitterConfigWidget implements TakesValue<AutonomousParticleEmitterConfig>, IsElement {
    @Inject
    @AutoBound
    private DataBinder<AutonomousParticleEmitterConfig> dataBinder;
    @Inject
    @DataField
    private TableRow autonomousTr;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @Bound
    @DataField
    private Input emittingDelay;
    @Inject
    @Bound
    @DataField
    private Input emittingCount;
    @Inject
    @Bound
    @DataField
    private Input generationRandomDistance;
    @Inject
    @Bound
    @DataField
    private Input startTime;
    @Inject
    @Bound
    @DataField
    private Input timeToLive;
    @Inject
    @Bound
    @DataField
    private VertexBox velocity;
    @Inject
    @Bound
    @DataField
    private Input directionSpeed;
    @Inject
    @Bound(property = "particleConfig.particleShapeConfigId")
    @DataField
    private Input particleShapeConfigId;
    @Inject
    @Bound(property = "particleConfig.particleXColorRampOffsetIndex")
    @DataField
    private Input colorRampOffsetIndex;
    @Inject
    @Bound(property = "particleConfig.particleGrowFrom")
    @DataField
    private Input particleGrowFrom;
    @Inject
    @Bound(property = "particleConfig.particleGrowTo")
    @DataField
    private Input particleGrowTo;
    @Inject
    @Bound(property = "particleConfig.timeToLive")
    @DataField
    private Input particleConfigTimeToLive;
    @Inject
    @Bound(property = "particleConfig.timeToLiveRandomPart")
    @DataField
    private Input timeToLiveRandomPart;
    @Inject
    @Bound(property = "particleConfig.velocity")
    @DataField
    private VertexBox particleConfigVelocity;
    @Inject
    @Bound(property = "particleConfig.velocityRandomPart")
    @DataField
    private VertexBox velocityRandomPart;
    @Inject
    @Bound(property = "particleConfig.acceleration")
    @DataField
    private VertexBox acceleration;

    @Override
    public void setValue(AutonomousParticleEmitterConfig autonomousParticleEmitterConfig) {
        dataBinder.setModel(autonomousParticleEmitterConfig);
    }

    @Override
    public AutonomousParticleEmitterConfig getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return autonomousTr;
    }
}
