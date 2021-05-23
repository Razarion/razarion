package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BoxItemTypeCrudSidebar extends AbstractCrudeParentSidebar<BoxItemType, BoxItemTypePropertyPanel> {
    // private Logger logger = Logger.getLogger(BoxItemTypeCrudSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private BoxItemTypeCrud boxItemTypeCrud;
    @Inject
    private Instance<BoxItemTypePropertyPanel> propertyPanelInstance;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
//        getEditorPanel().addButton("Restart", () -> provider.call(ignore -> {
//        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.reloadStatic() failed: ")).reloadStatic());
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
