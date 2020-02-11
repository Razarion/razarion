package com.btxtech.client.editor.sidebar;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.common.client.dom.TableCell;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.common.client.dom.Window;
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
    private Button deleteButton; // TODO use elemental2
    @Inject
    @DataField
    private Button saveButton; // TODO use elemental2
    @Inject
    @DataField
    private HTMLButtonElement closeButton;
    @Inject
    @DataField
    private TableRow buttonTableRow; // TODO use elemental2
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

    public AbstractEditor getContent() {
        return (AbstractEditor) content.getWidget();
    }

    Button getSaveButton() {
        return saveButton;
    }

    Button getDeleteButton() {
        return deleteButton;
    }

    public HTMLButtonElement getCloseButton() {
        return closeButton;
    }

    public void addButton(String text, Runnable clickCallback) {
        TableCell tableCell = (TableCell) Window.getDocument().createElement("td");
        buttonTableRow.appendChild(tableCell);
        org.jboss.errai.common.client.dom.Button button = (org.jboss.errai.common.client.dom.Button) Window.getDocument().createElement("button");
        button.setInnerHTML(text);
        tableCell.appendChild(button);
        button.addEventListener("click", event -> clickCallback.run(), false);
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        abstractEditor.onClose();
        mainPanelService.removeEditorPanel(this);
    }
}
