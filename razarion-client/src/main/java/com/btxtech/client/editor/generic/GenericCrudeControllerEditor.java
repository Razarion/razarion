package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.rest.CrudController;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class GenericCrudeControllerEditor extends AbstractCrudeParentSidebar<GenericObjectNameIdProvider, GenericPropertyPanel> {
    private GenericCrudController genericCrudController  = new GenericCrudController();

    public void setCrudControllerClass(Class<? extends CrudController> crudControllerClass) {
        genericCrudController.init(crudControllerClass);
    }

    @Override
    protected CrudEditor<GenericObjectNameIdProvider> getCrudEditor() {
        return genericCrudController;
    }

    @Override
    protected GenericPropertyPanel createPropertyPanel() {
        return null;
    }
}
