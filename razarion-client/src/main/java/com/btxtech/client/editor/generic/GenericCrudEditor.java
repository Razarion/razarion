package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.client.editor.generic.custom.CustomWidget;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.rest.CrudController;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class GenericCrudEditor extends AbstractCrudeParentSidebar<ObjectNameIdProvider, RootPropertySection> {
    @Inject
    private GenericCrudControllerEditor genericCrudControllerEditor;
    @Inject
    private Instance<RootPropertySection> genericPropertyPanelInstance;
    private Class<? extends CustomWidget> customWidgetClass;

    public void setCrudControllerClass(Class<? extends CrudController> crudControllerClass, Class<? extends CustomWidget> customWidgetClass) {
        this.customWidgetClass = customWidgetClass;
        genericCrudControllerEditor.init(crudControllerClass);
    }

    @Override
    protected CrudEditor<ObjectNameIdProvider> getCrudEditor() {
        return genericCrudControllerEditor;
    }

    @Override
    protected RootPropertySection createPropertyPanel() {
        RootPropertySection rootPropertySection = genericPropertyPanelInstance.get();
        rootPropertySection.setCustomWidgetClass(customWidgetClass);
        return rootPropertySection;
    }
}
