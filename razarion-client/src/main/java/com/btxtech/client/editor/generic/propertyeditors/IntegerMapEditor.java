package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Map;

@Templated("IntegerMapEditor.html#div")
public class IntegerMapEditor extends AbstractPropertyEditor<Map<Integer, Integer>> {
    // private Logger logger = Logger.getLogger(ListEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<IntegerMapEditorEntry> entryInstance;
    @Inject
    @DataField
    private HTMLDivElement div;
    @Inject
    @DataField
    private HTMLTableElement childTable;
    @Inject
    @DataField
    private HTMLButtonElement createButton;

    @Override
    public void showValue() {
        display();
    }

    public void display() {
        Elemental2Utils.removeAllChildren(childTable);
        Map<Integer, Integer> map = getPropertyValue();
        if (map != null) {
            map.forEach((key, value) -> {
                IntegerMapEditorEntry mapEditorEntry = entryInstance.get();
                mapEditorEntry.init(key, value, map, this);
                childTable.appendChild(mapEditorEntry.getElement());
            });
        }
    }

    @EventHandler("createButton")
    private void onCreateButtonClicked(ClickEvent e) {
        try {
            getPropertyValue().put(null, null);
            display();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }


    @Override
    public HTMLElement getElement() {
        return div;
    }
}
