package com.btxtech.client.editor.sidebar;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
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
@Templated("SideBarPanel.html#sideBarPanel")
public class SideBarPanel implements IsElement {
    @Inject
    private Instance<LeftSideBarContent> leftSideBarContentInstance;
    @Inject
    private LeftSideBarManager leftSideBarManager;
    @Inject
    @DataField
    private HTMLDivElement sideBarPanel;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private Button deleteButton;
    @Inject
    @DataField
    private Button saveButton;
    @Inject
    @DataField
    private Button closeButton;
    @Inject
    @DataField
    private Button backButton;
    @Inject
    @DataField
    private TableRow buttonTableRow;
    private LeftSideBarContent leftSideBarContent;

    @PostConstruct
    public void init() {
        sideBarPanel.style.zIndex = ZIndexConstants.EDITOR_SIDE_BAR;
        GwtUtils.preventContextMenu(sideBarPanel);
    }

    @Override
    public HTMLElement getElement() {
        return sideBarPanel;
    }

    void setContent(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
        if (leftSideBarContent != null) {
            leftSideBarContent.onClose();
        }
        leftSideBarContent = leftSideBarContentInstance.select(leftSideBarContentClass).get();
        leftSideBarContent.init(this);
        content.setWidget(leftSideBarContent);
    }

    public LeftSideBarContent getContent() {
        return (LeftSideBarContent) content.getWidget();
    }

    Button getSaveButton() {
        return saveButton;
    }

    Button getDeleteButton() {
        return deleteButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public Button getBackButton() {
        return backButton;
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
        leftSideBarContent.onClose();
        leftSideBarManager.close();
    }

    @EventHandler("backButton")
    private void backButtonClick(ClickEvent event) {
        leftSideBarContent.onClose();
        leftSideBarManager.pop();
    }
}
