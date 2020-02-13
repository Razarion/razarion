package com.btxtech.client.editor.sidebar;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.rest.CrudController;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.SimplePanel;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.05.2016.
 */
@Templated("EditorPanel.html#editorPanel")
public class EditorPanel implements IsElement {
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private Instance<AbstractEditor> leftSideBarContentInstance;
    @Inject
    @DataField
    private HTMLDivElement editorPanel;
    @Inject
    @DataField
    private SimplePanel content; // TODO use elemental2
    @Inject
    @DataField
    private HTMLButtonElement deleteButton;
    @Inject
    @DataField
    private HTMLButtonElement saveButton;
    @Inject
    @DataField
    private HTMLButtonElement closeButton;
    @Inject
    @DataField
    private HTMLTableRowElement buttonTableRow;
    private AbstractEditor abstractEditor;

    @PostConstruct
    public void init() {
        editorPanel.style.zIndex = ZIndexConstants.EDITOR_SIDE_BAR;
        GwtUtils.preventContextMenu(editorPanel);
    }

    @Override
    public HTMLElement getElement() {
        return editorPanel;
    }

    public void setContent(Class<? extends AbstractEditor> leftSideBarContentClass) {
        if (abstractEditor != null) {
            abstractEditor.onClose();
        }
        abstractEditor = leftSideBarContentInstance.select(leftSideBarContentClass).get();
        abstractEditor.init(this);
        content.setWidget(abstractEditor);
    }

    public void setGenericCrud(Class<? extends CrudController> crudControllerClass) {

    }

    public AbstractEditor getContent() {
        return (AbstractEditor) content.getWidget();
    }

    public HTMLButtonElement getSaveButton() {
        return saveButton;
    }

    public HTMLButtonElement getDeleteButton() {
        return deleteButton;
    }

    public void addButton(String text, Runnable clickCallback) {
        HTMLTableCellElement tableCell = (HTMLTableCellElement) DomGlobal.document.createElement("td");
        buttonTableRow.appendChild(tableCell);
        HTMLButtonElement button = (HTMLButtonElement) DomGlobal.document.createElement("button");
        button.innerHTML = text;
        tableCell.appendChild(button);
        button.addEventListener("click", event -> clickCallback.run(), false);
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        abstractEditor.onClose();
        mainPanelService.removeEditorPanel(this);
    }
}
