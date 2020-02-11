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
@Templated("SideBarPanel.html#sideBarPanel")
public class SideBarPanel implements IsElement {
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private Instance<LeftSideBarContent> leftSideBarContentInstance;
    @Inject
    @DataField
    private HTMLDivElement sideBarPanel;
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

    public void setContent(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
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
        leftSideBarContent.onClose();
        mainPanelService.removeEditorPanel(this);
    }
}
