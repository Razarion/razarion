package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.UIEvent;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Map;

@Templated("IntegerMapEditorEntry.html#mapEditorEntry")
public class IntegerMapEditorEntry implements IsElement {
    // private Logger logger = Logger.getLogger(MapEditorEntry.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableRowElement mapEditorEntry;
    @Inject
    @DataField
    private HTMLInputElement keyInput;
    @Inject
    @DataField
    private HTMLInputElement valueInput;
    @Inject
    @DataField
    private HTMLButtonElement deleteButton;
    private IntegerMapEditor mapEditor;
    private Map<Integer, Integer> map;
    private Integer key;

    public void init(Integer key, Integer value, Map<Integer, Integer> map, IntegerMapEditor mapEditor) {
        this.map = map;
        this.mapEditor = mapEditor;
        // TODO AbstractPropertyEditor abstractPropertyEditor = propertyEditorInstance.select(abstractPropertyModel.getEditorClass()).get();
        // TODO abstractPropertyEditor.init(abstractPropertyModel);
        // TODO propertyEditor.appendChild(abstractPropertyEditor.getElement());
        this.key = key;
        if (key != null) {
            keyInput.value = key.toString();
        }
        if (value != null) {
            valueInput.value = value.toString();
        }
    }

    @EventHandler("deleteButton")
    private void onDeleteButtonClicked(@ForEvent("click") UIEvent e) {
        try {
            map.remove(key);
            mapEditor.display();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("keyInput")
    private void onKeyInputChanged(@ForEvent("input") Event e) {
        try {
            Integer value = map.remove(key);
            key = Integer.parseInt(keyInput.value);
            map.put(key, value);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("valueInput")
    private void onValueInputChanged(@ForEvent("input") Event e) {
        try {
            map.put(key, Integer.parseInt(valueInput.value));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return mapEditorEntry;
    }
}
