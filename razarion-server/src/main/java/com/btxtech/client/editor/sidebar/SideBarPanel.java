package com.btxtech.client.editor.sidebar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 03.05.2016.
 */
@Templated("SideBarPanel.html#sideBarPanel")
public class SideBarPanel extends Composite {
    @Inject
    private LeftSideBar leftSideBar;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private Button closeButton;

    public void setContent(LeftSideBarContent leftSideBarContent) {
        content.setWidget(leftSideBarContent);
    }

    public LeftSideBarContent getContent() {
        return (LeftSideBarContent) content.getWidget();
    }

    @EventHandler("closeButton")
    private void closeButtonButtonClick(ClickEvent event) {
        leftSideBar.onClose((LeftSideBarContent) content.getWidget());
    }

}
