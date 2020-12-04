package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("PlaceConfigEditor.html#placeConfigPanel")
public class PlaceConfigEditor extends AbstractPropertyEditor<PlaceConfig>{
    @Inject
    @DataField
    private HTMLDivElement placeConfigPanel;
    @Inject
    @DataField
    private PlaceConfigWidget placeConfigWidget;

    @Override
    protected void showValue() {
        placeConfigWidget.init(getPropertyValue(), this::setPropertyValue);
    }

    @Override
    public HTMLElement getElement() {
        return placeConfigPanel;
    }
}
