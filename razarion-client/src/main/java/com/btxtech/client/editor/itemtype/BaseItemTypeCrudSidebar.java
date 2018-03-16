package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BaseItemTypeCrudSidebar extends AbstractCrudeParentSidebar<BaseItemType, BaseItemTypePropertyPanel> {
    // private Logger logger = Logger.getLogger(BaseItemTypeCrudSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private BaseItemTypeCrud baseItemTypeCrud;
    @Inject
    private Instance<BaseItemTypePropertyPanel> propertyPanelInstance;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.reloadStatic() failed: ")).reloadStatic());
    }

    @Override
    protected CrudEditor<BaseItemType> getCrudEditor() {
        return baseItemTypeCrud;
    }

    @Override
    protected BaseItemTypePropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
