package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.I18nStringWidget;
import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.uiservice.renderer.task.BoxItemRenderTask;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
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
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DReferenceFiled;
    @Inject
    @Bound
    @DataField
    private DoubleBox radius;
    @Inject
    @DataField
    private I18nStringWidget i18nName;
    @Inject
    @DataField
    private I18nStringWidget i18nDescription;
    @Inject
    @DataField
    private ImageItemWidget thumbnail;
    @Inject
    @Bound
    @DataField
    private NumberInput ttl;
    @Inject
    @Bound
    @DataField
    private CheckboxInput fixVerticalNorm;
    @Inject
    @DataField
    private ChildTable<BoxItemTypePossibility> boxItemTypePossibilities;

    @Override
    public void init(BoxItemType boxItemType) {
        boxItemTypeDataBinder.setModel(boxItemType);
        shape3DReferenceFiled.init(boxItemType.getShape3DId(), shape3DId -> {
            boxItemType.setShape3DId(shape3DId);
            boxItemRenderTask.onBoxItemTypeChanged(boxItemType);
        });
        i18nName.init(boxItemType.getI18nName(), boxItemType::setI18nName);
        i18nDescription.init(boxItemType.getI18nDescription(), boxItemType::setI18nDescription);
        thumbnail.setImageId(boxItemType.getThumbnail(), boxItemType::setThumbnail);
        boxItemTypePossibilities.init(boxItemType.getBoxItemTypePossibilities(), boxItemType::setBoxItemTypePossibilities, BoxItemTypePossibility::new, BoxItemTypePossibilityPanel.class);
    }

    @Override
    public BoxItemType getConfigObject() {
        return boxItemTypeDataBinder.getModel();
    }
}
