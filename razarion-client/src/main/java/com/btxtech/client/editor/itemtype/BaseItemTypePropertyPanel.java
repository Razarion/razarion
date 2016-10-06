package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
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
@Templated("BaseItemTypePropertyPanel.html#sync-base-item-property-panel")
public class BaseItemTypePropertyPanel extends AbstractPropertyPanel<BaseItemType> {
    @Inject
    private BaseItemRenderTask baseItemRenderTask;
    @Inject
    @AutoBound
    private DataBinder<BaseItemType> baseItemTypeDataBinder;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Shape3DReferenceFiled spawnShape3DIdReferenceFiled;

    @Override
    public void init(BaseItemType baseItemType) {
        baseItemTypeDataBinder.setModel(baseItemType);
        shape3DReferenceFiled.init(baseItemType.getShape3DId(), shape3DId -> {
            baseItemType.setShape3DId(shape3DId);
            baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
        });
        spawnShape3DIdReferenceFiled.init(baseItemType.getSpawnShape3DId(), shape3DId -> {
            baseItemType.setSpawnShape3DId(shape3DId);
            baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
        });
    }

    @Override
    public BaseItemType getConfigObject() {
        return baseItemTypeDataBinder.getModel();
    }
}
