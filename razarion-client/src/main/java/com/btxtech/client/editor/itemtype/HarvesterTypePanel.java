package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.client.guielements.VertexBox;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
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
@Templated("HarvesterTypePanel.html#harvesterTypePanel")
public class HarvesterTypePanel extends Composite implements TakesValue<HarvesterType> {
    @Inject
    @AutoBound
    private DataBinder<HarvesterType> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput range;
    @Inject
    @Bound
    @DataField
    private NumberInput progress;
    @Inject
    @Bound
    @DataField
    private VertexBox animationOrigin;
    @Inject
    @DataField
    private Shape3DReferenceFiled animationShape3dId;

    @Override
    public void setValue(HarvesterType harvesterType) {
        dataBinder.setModel(harvesterType);
        animationShape3dId.init(harvesterType.getAnimationShape3dId(), harvesterType::setAnimationShape3dId);
    }

    @Override
    public HarvesterType getValue() {
        return dataBinder.getModel();
    }
}
