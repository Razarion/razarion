package com.btxtech.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
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
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private Button closeButton;

    public void setContent(Widget widget) {
        content.setWidget(widget);
    }

    @EventHandler("closeButton")
    private void closeButtonButtonClick(ClickEvent event) {
        content.clear();
        getElement().getStyle().setDisplay(Style.Display.NONE);
    }

}
