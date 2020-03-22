package com.btxtech.client.editor.generic.propertyeditors;

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
import java.util.logging.Logger;

@Templated("ListEditor.html#div")
public class ListEditor extends AbstractPropertyEditor {
    private Logger logger = Logger.getLogger(ListEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<ListEditorEntry> entryInstance;
    @Inject
    @DataField
    private HTMLDivElement div;
    @Inject
    @DataField
    private HTMLTableElement childTableTable;
    @Inject
    @DataField
    private HTMLButtonElement createButton;

    @Override
    public void showValue() {
    }

    @EventHandler("createButton")
    private void onCreateButtonClicked(ClickEvent e) {
//        try {
//            Object property = hasProperties.get(propertyName);
//            logger.severe("property: " + property);
//            PropertyType propertyType = hasProperties.getBeanProperties().get(propertyName);
//            logger.severe("propertyType: " + propertyType);
//            Object o = BindableProxyFactory.getBindableProxy(propertyType);
//            logger.severe("o: " + o);
//            // hasProperties.set(propertyName, enumEntry);
//        } catch (Throwable t) {
//            exceptionHandler.handleException(t);
//        }
    }


    @Override
    public HTMLElement getElement() {
        return div;
    }
}
