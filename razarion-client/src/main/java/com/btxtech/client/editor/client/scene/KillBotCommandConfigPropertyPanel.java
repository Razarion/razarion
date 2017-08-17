package com.btxtech.client.editor.client.scene;

import com.btxtech.shared.dto.KillBotCommandConfig;
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
 * on 17.08.2017.
 */
@Templated("KillBotCommandConfigPropertyPanel.html#killBotCommandConfigPropertyPanel")
public class KillBotCommandConfigPropertyPanel extends Composite implements TakesValue<KillBotCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<KillBotCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;

    @Override
    public void setValue(KillBotCommandConfig killBotCommandConfig) {
        dataBinder.setModel(killBotCommandConfig);
    }

    @Override
    public KillBotCommandConfig getValue() {
        return dataBinder.getModel();
    }
}
