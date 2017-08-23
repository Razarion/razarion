package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 23.08.2017.
 */
@Templated("DemolitionStepEffectPanel.html#demolitionStepEffectPanel")
public class DemolitionStepEffectPanel extends Composite implements TakesValue<DemolitionStepEffect> {
    private DemolitionStepEffect demolitionStepEffect;
    @Inject
    @DataField
    private ChildTable<DemolitionParticleConfig> demolitionParticleConfigs;

    @Override
    public void setValue(DemolitionStepEffect demolitionStepEffect) {
        this.demolitionStepEffect = demolitionStepEffect;
        demolitionParticleConfigs.init(demolitionStepEffect.getDemolitionParticleConfigs(), demolitionStepEffect::setDemolitionParticleConfigs, DemolitionParticleConfig::new, DemolitionParticleConfigPanel.class);
    }

    @Override
    public DemolitionStepEffect getValue() {
        return demolitionStepEffect;
    }
}
