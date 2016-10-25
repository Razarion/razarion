package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.uiservice.renderer.task.BoxItemRenderTask;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("BoxItemTypePropertyPanel.html#box-item-property-panel")
public class BoxItemTypePropertyPanel extends AbstractPropertyPanel<BoxItemType> {
    @Inject
    private BoxItemRenderTask boxItemRenderTask;
    @Inject
    @AutoBound
    private DataBinder<BoxItemType> boxItemTypeDataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private TextBox name;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DReferenceFiled;
    @Inject
    @Bound
    @DataField
    private DoubleBox radius;

    @Override
    public void init(BoxItemType boxItemType) {
        boxItemTypeDataBinder.setModel(boxItemType);
        shape3DReferenceFiled.init(boxItemType.getShape3DId(), shape3DId -> {
            boxItemType.setShape3DId(shape3DId);
            boxItemRenderTask.onBoxItemTypeChanged(boxItemType);
        });
    }

    @Override
    public BoxItemType getConfigObject() {
        return boxItemTypeDataBinder.getModel();
    }
}
