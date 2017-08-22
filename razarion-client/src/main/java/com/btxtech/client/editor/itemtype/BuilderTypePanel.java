package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.itemtype.baselist.BaseItemTypeListWidget;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.client.guielements.VertexBox;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
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
@Templated("BuilderTypePanel.html#builderTypePanel")
public class BuilderTypePanel extends Composite implements TakesValue<BuilderType> {
    @Inject
    @AutoBound
    private DataBinder<BuilderType> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput range;
    @Inject
    @Bound
    @DataField
    private NumberInput progress;
    @Inject
    @DataField
    private BaseItemTypeListWidget ableToBuildIds;
    @Inject
    @Bound
    @DataField
    private VertexBox animationOrigin;
    @Inject
    @DataField
    private Shape3DReferenceFiled animationShape3dId;

    @Override
    public void setValue(BuilderType builderType) {
        dataBinder.setModel(builderType);
        ableToBuildIds.init(builderType.getAbleToBuildIds(), builderType::setAbleToBuildIds);
        animationShape3dId.init(builderType.getAnimationShape3dId(), builderType::setAnimationShape3dId);
    }

    @Override
    public BuilderType getValue() {
        return dataBinder.getModel();
    }
}
