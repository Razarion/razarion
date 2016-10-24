package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 22.11.2015.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class SlopeConfigCrudSidebar extends AbstractCrudeParentSidebar<SlopeConfig, SlopeConfigPropertyPanel> {
    // private Logger logger = Logger.getLogger(SlopeConfigCrudSidebar.class.getName());
    @Inject
    private SlopeConfigCrud slopeConfigCrud;
    @Inject
    private Instance<SlopeConfigPropertyPanel> propertyPanelInstance;

    @Override
    protected CrudEditor<SlopeConfig> getCrudEditor() {
        return slopeConfigCrud;
    }

    @Override
    protected SlopeConfigPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
