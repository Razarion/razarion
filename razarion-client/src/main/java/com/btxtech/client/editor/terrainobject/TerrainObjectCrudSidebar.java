package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.shared.dto.TerrainObjectConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class TerrainObjectCrudSidebar extends AbstractCrudeParentSidebar<TerrainObjectConfig, TerrainObjectPropertyPanel> {
    @Inject
    private TerrainObjectCrud terrainObjectCrud;
    @Inject
    private Instance<TerrainObjectPropertyPanel> configPanelInstance;

    @Override
    protected TerrainObjectCrud getCrudEditor() {
        return terrainObjectCrud;
    }

    @Override
    protected TerrainObjectPropertyPanel createPropertyPanel() {
        return configPanelInstance.get();
    }
}
