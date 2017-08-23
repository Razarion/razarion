package com.btxtech.client.editor.itemtype;

import com.btxtech.client.guielements.VertexBox;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 23.08.2017.
 */
@Templated("DemolitionParticleConfigPanel.html#demolitionParticleConfigPanel")
public class DemolitionParticleConfigPanel extends Composite implements TakesValue<DemolitionParticleConfig> {
    @Inject
    @AutoBound
    private DataBinder<DemolitionParticleConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput particleConfigId;
    @Inject
    @Bound
    @DataField
    private VertexBox position;

    @Override
    public void setValue(DemolitionParticleConfig demolitionParticleConfig) {
        dataBinder.setModel(demolitionParticleConfig);
    }

    @Override
    public DemolitionParticleConfig getValue() {
        return dataBinder.getModel();
    }
}
