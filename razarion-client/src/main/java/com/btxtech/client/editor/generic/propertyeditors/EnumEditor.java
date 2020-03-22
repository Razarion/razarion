package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Templated("EnumEditor.html#select")
public class EnumEditor extends AbstractPropertyEditor<Enum> {
    // private Logger logger = Logger.getLogger(EnumEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLSelectElement select;
    private List<Enum> enumOptions;

    @Override
    public void showValue() {
        enumOptions = Arrays.stream(getPropertyModel().getPropertyClass().getEnumConstants())
                .sorted(Comparator.comparing(Object::toString)).map(o -> (Enum) o).collect(Collectors.toList());

        enumOptions.forEach(o -> {
            HTMLOptionElement option = (HTMLOptionElement) DomGlobal.document.createElement("option");
            option.text = o.toString();
            select.add(option);
        });

        Enum value = getPropertyValue();
        if (value != null) {
            select.selectedIndex = enumOptions.indexOf(value);
        } else {
            select.selectedIndex = -1;
        }
    }

    @EventHandler("select")
    private void onSelectChanged(@ForEvent("change") Event e) {
        try {
            setPropertyValue(enumOptions.get((int) select.selectedIndex));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }


    @Override
    public HTMLElement getElement() {
        return select;
    }
}
