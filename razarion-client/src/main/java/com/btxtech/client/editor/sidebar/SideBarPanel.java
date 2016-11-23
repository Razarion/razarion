package com.btxtech.client.editor.sidebar;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
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
public class SideBarPanel extends Composite {
    @Inject
    private Instance<LeftSideBarContent> leftSideBarContentInstance;
    @Inject
    private LeftSideBarManager leftSideBarManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button deleteButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button saveButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button closeButton;
    private LeftSideBarContent leftSideBarContent;

//    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    @DataField
//    private Button closeButton;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.EDITOR_SIDE_BAR);
    }

    void setContent(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
        if(leftSideBarContent != null) {
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

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        leftSideBarContent.onClose();
        leftSideBarManager.close();
    }

}
