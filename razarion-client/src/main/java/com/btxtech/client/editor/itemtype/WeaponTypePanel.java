package com.btxtech.client.editor.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("WeaponTypePanel.html#weaponTypePanel")
public class WeaponTypePanel extends Composite implements TakesValue<WeaponType> {
    @Inject
    @AutoBound
    private DataBinder<WeaponType> dataBinder;

    @Override
    public void setValue(WeaponType weaponType) {
        dataBinder.setModel(weaponType);
    }

    @Override
    public WeaponType getValue() {
        return dataBinder.getModel();
    }
}
