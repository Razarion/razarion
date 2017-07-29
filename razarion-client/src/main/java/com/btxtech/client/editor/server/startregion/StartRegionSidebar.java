package com.btxtech.client.editor.server.startregion;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.dto.StartRegionConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class StartRegionSidebar extends AbstractCrudeParentSidebar<StartRegionConfig, StartRegionPropertyPanel> {
    @Inject
    private Instance<StartRegionPropertyPanel> propertyPanelInstance;
    @Inject
    private StartRegionCrudEditor startRegionCrudEditor;

    @Override
    protected CrudEditor<StartRegionConfig> getCrudEditor() {
        return startRegionCrudEditor;
    }

    @Override
    protected StartRegionPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
