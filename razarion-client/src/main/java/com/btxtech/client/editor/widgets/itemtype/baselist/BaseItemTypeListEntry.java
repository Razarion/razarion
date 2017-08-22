package com.btxtech.client.editor.widgets.itemtype.baselist;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("BaseItemTypeListWidget.html#baseItemTypeRow")
public class BaseItemTypeListEntry implements TakesValue<BaseItemTypeListEntryModel>, IsElement {
    @Inject
    @DataField
    private TableRow baseItemTypeRow;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemType;
    @Inject
    @DataField
    private Button baseItemTypeListDeleteButton;
    private BaseItemTypeListEntryModel baseItemTypeListEntryModel;


    @Override
    public void setValue(BaseItemTypeListEntryModel baseItemTypeListEntryModel) {
        this.baseItemTypeListEntryModel = baseItemTypeListEntryModel;
        baseItemType.init(baseItemTypeListEntryModel.getBaseItemTypeId(), baseItemTypeListEntryModel::setBaseItemTypeId);
    }

    @Override
    public BaseItemTypeListEntryModel getValue() {
        return baseItemTypeListEntryModel;
    }

    @Override
    public HTMLElement getElement() {
        return baseItemTypeRow;
    }

    @EventHandler("baseItemTypeListDeleteButton")
    private void baseItemTypeListDeleteButtonClicked(ClickEvent event) {
        baseItemTypeListEntryModel.remove();
    }
}
