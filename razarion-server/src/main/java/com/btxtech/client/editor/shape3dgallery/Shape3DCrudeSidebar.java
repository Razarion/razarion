package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.framework.AbstractCrudeParent;
import com.btxtech.shared.datatypes.shape.Shape3D;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 17.08.2016.
 */
@Templated("../framework/AbstractCrudeParent.html#abstract-crud-parent")
public class Shape3DCrudeSidebar extends AbstractCrudeParent<Shape3D, Shape3DPropertyPanel> {
    // private Logger logger = Logger.getLogger(Shape3DCrudeSidebar.class.getName());
    @Inject
    private Shape3DCrud shape3DCrud;
    @Inject
    private Instance<Shape3DPropertyPanel> propertyPanelInstance;


    @Override
    protected Shape3DCrud getCrudEditor() {
        return shape3DCrud;
    }

    @Override
    protected Shape3DPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
