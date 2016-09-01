package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BaseItemTypeCrudSidebar extends AbstractCrudeParentSidebar<BaseItemType, BaseItemTypePropertyPanel> {
    @Inject
    private BaseItemTypeCrud baseItemTypeCrud;
    @Inject
    private Instance<BaseItemTypePropertyPanel> propertyPanelInstance;

    @Override
    protected CrudEditor<BaseItemType> getCrudEditor() {
        return baseItemTypeCrud;
    }

    @Override
    protected BaseItemTypePropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
