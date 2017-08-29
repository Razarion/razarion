package com.btxtech.client.editor.server.resource;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.client.editor.server.startregion.StartRegionPropertyPanel;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ResourceRegionSidebar extends AbstractCrudeParentSidebar<ResourceRegionConfig, ResourceRegionPropertyPanel> {
    private Logger logger = Logger.getLogger(ResourceRegionSidebar.class.getName());
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;
    @Inject
    private Instance<ResourceRegionPropertyPanel> propertyPanelInstance;
    @Inject
    private ResourceRegionCrudEditor resourceRegionCrudEditor;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Calling ServerGameEngineControlProvider.restartResourceRegions() failed: " + message, throwable);
            return false;
        }).restartResourceRegions());
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
