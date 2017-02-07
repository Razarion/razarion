package com.btxtech.client.editor.particle;

import com.btxtech.uiservice.particle.DependentParticleEmitterConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
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
public class EmitterPropertyWidget implements TakesValue<DependentParticleEmitterConfig>, IsElement {
    @Inject
    @AutoBound
    private DataBinder<DependentParticleEmitterConfig> dataBinder;
    @Inject
    @DataField
    private TableRow dependentTr;
    @Inject
    @Bound(property = "particleConfig.particleShapeConfigId")
    @DataField
    private Input particleShapeConfigId;
    @Inject
    @Bound(property = "particleConfig.particleGrow")
    @DataField
    private Input particleGrow;
    @Inject
    @Bound(property = "particleConfig.timeToLive")
    @DataField
    private Input timeToLive;
    @Inject
    @Bound(property = "particleConfig.timeToLiveRandomPart")
    @DataField
    private Input timeToLiveRandomPart;
    @Inject
    @Bound(property = "particleConfig.speedX")
    @DataField
    private Input speedX;
    @Inject
    @Bound(property = "particleConfig.speedXRandomPart")
    @DataField
    private Input speedXRandomPart;
    @Inject
    @Bound(property = "particleConfig.speedY")
    @DataField
    private Input speedY;
    @Inject
    @Bound(property = "particleConfig.speedYRandomPart")
    @DataField
    private Input speedYRandomPart;
    @Inject
    @Bound(property = "particleConfig.speedZ")
    @DataField
    private Input speedZ;
    @Inject
    @Bound(property = "particleConfig.speedZRandomPart")
    @DataField
    private Input speedZRandomPart;
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

    @Override
    public void setValue(DependentParticleEmitterConfig particleEmitterConfig) {
        dataBinder.setModel(particleEmitterConfig);
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
