package com.btxtech.client.editor;

import com.btxtech.client.editor.generic.custom.CustomWidget;
import com.btxtech.client.utils.DomConstants;
import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Arrays;

@Templated("EditorMenuButtonSection.html#table")
public class EditorMenuButtonSection implements IsElement {
    @Inject
    private EditorService editorService;
    @Inject
    private Instance<EditorMenuButton> buttonFactory;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableElement table;

    public static class CrudControllerButton {
        private Class<? extends CrudController> crudControllerClass;
        private String title;
        private Class<? extends CustomWidget> customWidgetClass;

        public CrudControllerButton(Class<? extends CrudController> crudControllerClass, String title, Class<? extends CustomWidget> customWidgetClass) {
            this.crudControllerClass = crudControllerClass;
            this.customWidgetClass = customWidgetClass;
            this.title = title;
        }
    }

    @Templated("EditorMenuButtonSection.html#row")
    public static class EditorMenuButton implements IsElement {
        @Inject
        @DataField
        private HTMLTableRowElement row;
        @Inject
        @DataField
        private HTMLButtonElement button;

        public void init(String title, EventListener eventListener) {
            button.textContent = title;
            button.addEventListener(DomConstants.Event.CLICK, eventListener);
        }

        @Override
        public HTMLElement getElement() {
            return row;
        }
    }


    public void showSection(Runnable beforeOpen, CrudControllerButton... crudControllerButtons) {
        Elemental2Utils.removeAllChildren(table);
        Arrays.stream(crudControllerButtons).forEach(crudControllerButton -> {
            EditorMenuButton editorMenuButton = buttonFactory.get();
            editorMenuButton.init(crudControllerButton.title, evt -> {
                try {
                    beforeOpen.run();
                    editorService.openGenericCrudEditor(crudControllerButton.crudControllerClass, crudControllerButton.title, crudControllerButton.customWidgetClass);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
                }
            });
            table.appendChild(editorMenuButton.getElement());
        });
    }

    @Override
    public HTMLElement getElement() {
        return table;
    }
}
