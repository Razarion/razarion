package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.childpanel.ChildContainer;
import com.btxtech.client.editor.widgets.itemtype.baselist.BaseItemTypeListWidget;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
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
 * on 22.08.2017.
 */
@Templated("WeaponTypePanel.html#weaponTypePanel")
public class WeaponTypePanel extends Composite implements TakesValue<WeaponType> {
    @Inject
    @AutoBound
    private DataBinder<WeaponType> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput range;
    @Inject
    @Bound
    @DataField
    private NumberInput damage;
    @Inject
    @Bound
    @DataField
    private NumberInput detonationRadius;
    @Inject
    @Bound
    @DataField
    private NumberInput reloadTime;
    @Inject
    @DataField
    private BaseItemTypeListWidget disallowedItemTypes;
    @Inject
    @Bound
    @DataField
    private NumberInput projectileSpeed;
    @Inject
    @DataField
    private Shape3DReferenceFiled projectileShape3DId;
    @Inject
    @Bound
    @DataField
    private NumberInput muzzleFlashParticleConfigId;
    @Inject
    @Bound
    @DataField
    private NumberInput detonationParticleConfigId;
    @Inject
    @DataField
    private ChildContainer<TurretType> turretType;

    @Override
    public void setValue(WeaponType weaponType) {
        dataBinder.setModel(weaponType);
        disallowedItemTypes.init(weaponType.getDisallowedItemTypes(), weaponType::disallowedItemTypes);
        turretType.init(weaponType.getTurretType(), weaponType::turretType, TurretType::new, TurretTypePanel.class);
        projectileShape3DId.init(weaponType.getProjectileShape3DId(), weaponType::projectileShape3DId);
    }

    @Override
    public WeaponType getValue() {
        return dataBinder.getModel();
    }
}
