package com.btxtech.client.editor.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.SpecialType;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("SpecialTypePanel.html#specialTypePanel")
public class SpecialTypePanel extends Composite implements TakesValue<SpecialType> {
    @Inject
    @AutoBound
    private DataBinder<SpecialType> dataBinder;
    @Inject
    @Bound
    @DataField
    private CheckboxInput miniTerrain;

    @Override
    public void setValue(SpecialType specialType) {
        dataBinder.setModel(specialType);
    }

    @Override
    public SpecialType getValue() {
        return dataBinder.getModel();
    }
}
