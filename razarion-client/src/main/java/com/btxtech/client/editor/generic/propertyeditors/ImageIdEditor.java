package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("ImageIdEditor.html#imageIdPanel")
public class ImageIdEditor extends AbstractPropertyEditor<Integer> {
    // private Logger logger = Logger.getLogger(ImageEditor.class.getName());
    @Inject
    @DataField
    private HTMLDivElement imageIdPanel;
    @Inject
    @DataField
    private ImageItemWidget imageItemWidget;

    @Override
    public void showValue() {
        if (getPropertyValue() != null) {
            imageItemWidget.setImageId(getPropertyValue(), this::setPropertyValue);
        }
    }

    @Override
    public HTMLElement getElement() {
        return imageIdPanel;
    }
}
