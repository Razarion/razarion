package com.btxtech.client.editor.clip;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.shared.dto.ClipConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.10.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class ClipCrudeSidebar extends AbstractCrudeParentSidebar<ClipConfig, ClipPropertyPanel> {
    // private Logger logger = Logger.getLogger(ClipCrudeSidebar.class.getName());
    @Inject
    private ClipCrud clipCrud;
    @Inject
    private Instance<ClipPropertyPanel> propertyPanelInstance;

    @Override
    protected ClipCrud getCrudEditor() {
        return clipCrud;
    }

    @Override
    protected ClipPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
