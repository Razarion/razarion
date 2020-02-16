package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.rest.CrudController;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class GenericCrudeControllerEditor extends AbstractCrudeParentSidebar<ObjectNameIdProvider, GenericPropertyPanel> {
    private GenericCrudControllerEditor genericCrudControllerEditor = new GenericCrudControllerEditor();
    @Inject
    private Instance<GenericPropertyPanel> genericPropertyPanelInstance;

    public void setCrudControllerClass(Class<? extends CrudController> crudControllerClass) {
        genericCrudControllerEditor.init(crudControllerClass);
    }

    @Override
    protected CrudEditor<ObjectNameIdProvider> getCrudEditor() {
        return genericCrudControllerEditor;
    }

    @Override
    protected GenericPropertyPanel createPropertyPanel() {
        return genericPropertyPanelInstance.get();
    }
}
