package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ResourceItemTypeCrudSidebar extends AbstractCrudeParentSidebar<ResourceItemType, ResourceItemTypePropertyPanel> {
    private Logger logger = Logger.getLogger(ResourceItemTypeCrudSidebar.class.getName());
    @Inject
    private ResourceItemTypeCrud resourceItemTypeCrud;
    @Inject
    private Instance<ResourceItemTypePropertyPanel> propertyPanelInstance;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Calling ServerGameEngineControlProvider.reloadStatic() failed: " + message, throwable);
            return false;
        }).reloadStatic());
    }

    @Override
    protected CrudEditor<ResourceItemType> getCrudEditor() {
        return resourceItemTypeCrud;
    }

    @Override
    protected ResourceItemTypePropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
