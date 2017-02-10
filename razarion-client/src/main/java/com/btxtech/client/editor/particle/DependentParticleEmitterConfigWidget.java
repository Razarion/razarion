package com.btxtech.client.editor.particle;

import com.btxtech.client.guielements.VertexBox;
import com.btxtech.uiservice.particle.DependentParticleEmitterConfig;
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
@Templated("ParticlePropertyPanel.html#dependentTr")
public class DependentParticleEmitterConfigWidget implements TakesValue<DependentParticleEmitterConfig>, IsElement {
    @Inject
    @AutoBound
    private DataBinder<DependentParticleEmitterConfig> dataBinder;
    @Inject
    @DataField
    private TableRow dependentTr;
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
    private Input timeToLive;
    @Inject
    @Bound(property = "particleConfig.timeToLiveRandomPart")
    @DataField
    private Input timeToLiveRandomPart;
    @Inject
    @Bound(property = "particleConfig.velocity")
    @DataField
    private VertexBox velocity;
    @Inject
    @Bound(property = "particleConfig.velocityRandomPart")
    @DataField
    private VertexBox velocityRandomPart;
    @Inject
    @Bound(property = "particleConfig.acceleration")
    @DataField
    private VertexBox acceleration;

    @Override
    public void setValue(DependentParticleEmitterConfig dependentParticleEmitterConfig) {
        dataBinder.setModel(dependentParticleEmitterConfig);
    }

    @Override
    public DependentParticleEmitterConfig getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return dependentTr;
    }
}
