package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Templated("EnumEditor.html#select")
public class EnumEditor implements GenericPropertyEditor {
    private Logger logger = Logger.getLogger(EnumEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLSelectElement select;
    private List<Enum> enumOptions;
    private String propertyName;
    private HasProperties hasProperties;

    @Override
    public void init(String propertyName, Class propertyClass, HasProperties hasProperties) {
        this.propertyName = propertyName;
        this.hasProperties = hasProperties;

        enumOptions = Arrays.stream(propertyClass.getEnumConstants())
                .sorted(Comparator.comparing(Object::toString)).map(o -> (Enum)o).collect(Collectors.toList());

        enumOptions.forEach(o -> {
            HTMLOptionElement option = (HTMLOptionElement) DomGlobal.document.createElement("option");
            option.text = o.toString();
            select.add(option);
        });

        Enum value = (Enum) hasProperties.get(propertyName);
        if(value != null) {
            select.selectedIndex = enumOptions.indexOf(value);
        } else {
            select.selectedIndex = -1;
        }
    }

    @EventHandler("select")
    private void onsSelectChanged(@ForEvent("change") Event e) {
        try {
            Enum enumEntry = enumOptions.get((int)select.selectedIndex);
            hasProperties.set(propertyName, enumEntry);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }


    @Override
    public HTMLElement getElement() {
        return select;
    }
}
