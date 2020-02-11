package com.btxtech.client.editor.server.resource;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ResourceRegionSidebar extends AbstractCrudeParentSidebar<ResourceRegionConfig, ResourceRegionPropertyPanel> {
    // private Logger logger = Logger.getLogger(ResourceRegionSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;
    @Inject
    private Instance<ResourceRegionPropertyPanel> propertyPanelInstance;
    @Inject
    private ResourceRegionCrudEditor resourceRegionCrudEditor;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getEditorPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.restartResourceRegions() failed: ")).restartResourceRegions());
    }

    @Override
    protected CrudEditor<ResourceRegionConfig> getCrudEditor() {
        return resourceRegionCrudEditor;
    }

    @Override
    protected ResourceRegionPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
