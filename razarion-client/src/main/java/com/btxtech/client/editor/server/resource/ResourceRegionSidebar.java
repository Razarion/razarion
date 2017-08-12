package com.btxtech.client.editor.server.resource;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.client.editor.server.startregion.StartRegionPropertyPanel;
import com.btxtech.shared.dto.ResourceRegionConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ResourceRegionSidebar extends AbstractCrudeParentSidebar<ResourceRegionConfig, ResourceRegionPropertyPanel> {
    @Inject
    private Instance<ResourceRegionPropertyPanel> propertyPanelInstance;
    @Inject
    private ResourceRegionCrudEditor resourceRegionCrudEditor;

    @Override
    protected CrudEditor<ResourceRegionConfig> getCrudEditor() {
        return resourceRegionCrudEditor;
    }

    @Override
    protected ResourceRegionPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
