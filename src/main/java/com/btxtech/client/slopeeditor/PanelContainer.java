package com.btxtech.client.slopeeditor;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 22.11.2015.
 */
@Templated("PanelContainer.html#editorPanelContainer")
public class PanelContainer extends Composite {
    @Inject
    private Instance<SlopePanel> plateauPanelInstance;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private Button closeButton;

    public void showSlopeEditor(int id) {
        content.clear();
        closeButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        SlopePanel slopePanel = plateauPanelInstance.get();
        slopePanel.init(id);
        content.setWidget(slopePanel);
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        content.clear();
        closeButton.getElement().getStyle().setDisplay(Style.Display.NONE);
    }
}
