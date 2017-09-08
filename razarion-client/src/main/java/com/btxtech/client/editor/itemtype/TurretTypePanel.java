package com.btxtech.client.editor.itemtype;

import com.btxtech.client.guielements.VertexBox;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 08.09.2017.
 */
@Templated("TurretTypePanel.html#turretTypePanel")
public class TurretTypePanel extends Composite implements TakesValue<TurretType> {
    @Inject
    @AutoBound
    private DataBinder<TurretType> dataBinder;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField
    private DoubleBox angleVelocity;
    @Inject
    @Bound
    @DataField
    private VertexBox torrentCenter;
    @Inject
    @Bound
    @DataField
    private VertexBox muzzlePosition;
    @Inject
    @Bound
    @DataField
    private Input shape3dMaterialId;

    @Override
    public void setValue(TurretType turretType) {
        dataBinder.setModel(turretType);
    }

    @Override
    public TurretType getValue() {
        return dataBinder.getModel();
    }
}
