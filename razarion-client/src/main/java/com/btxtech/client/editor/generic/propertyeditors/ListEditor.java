package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

@Templated("ListEditor.html#div")
public class ListEditor extends AbstractPropertyEditor<List> {
    // private Logger logger = Logger.getLogger(ListEditor.class.getName());
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
        Elemental2Utils.removeAllChildren(childTableTable);
        try {
            getPropertyModel().createListChildren(propertyModel -> {
                ListEditorEntry listEditorEntry = entryInstance.get();
                listEditorEntry.init(propertyModel);
                childTableTable.appendChild(listEditorEntry.getElement());
            });
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("createButton")
    private void onCreateButtonClicked(ClickEvent e) {
        try {
            BindableListWrapper bindableListWrapper = (BindableListWrapper) BindableProxyFactory.getBindableProxy(getPropertyValue());
            bindableListWrapper.add(new SceneConfig());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }


    @Override
    public HTMLElement getElement() {
        return div;
    }
}
