package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ResourceItemTypeCrudSidebar extends AbstractCrudeParentSidebar<ResourceItemType, ResourceItemTypePropertyPanel> {
    @Inject
    private ResourceItemTypeCrud resourceItemTypeCrud;
    @Inject
    private Instance<ResourceItemTypePropertyPanel> propertyPanelInstance;

    @Override
    protected CrudEditor<ResourceItemType> getCrudEditor() {
        return resourceItemTypeCrud;
    }

    @Override
    protected ResourceItemTypePropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
