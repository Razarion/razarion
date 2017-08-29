package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
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
public class BoxItemTypeCrudSidebar extends AbstractCrudeParentSidebar<BoxItemType, BoxItemTypePropertyPanel> {
    private Logger logger = Logger.getLogger(BoxItemTypeCrudSidebar.class.getName());
    @Inject
    private BoxItemTypeCrud boxItemTypeCrud;
    @Inject
    private Instance<BoxItemTypePropertyPanel> propertyPanelInstance;
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
    protected CrudEditor<BoxItemType> getCrudEditor() {
        return boxItemTypeCrud;
    }

    @Override
    protected BoxItemTypePropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
