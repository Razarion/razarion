package com.btxtech.client.cockpit.item;

import com.btxtech.uiservice.cockpit.item.ItemContainerPanel;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 06.12.2017.
 */
@Templated("ClientItemContainerPanel.html#itemContainerPanel")
public class ClientItemContainerPanel extends ItemContainerPanel implements IsElement {
    @Inject
    @DataField("itemContainerPanel")
    private Div div;
    @Inject
    @DataField
    private Span containingText;
    @Inject
    @DataField
    private Button unloadModeButton;

    @PostConstruct
    public void postConstruct() {
        unloadModeButton.setText(I18nHelper.getConstants().unloadButton());
        unloadModeButton.setTitle(I18nHelper.getConstants().unloadButtonTooltip());
    }

    @Override
    protected void updateGui(boolean enabled, int count) {
        if (enabled) {
            if (count == 1) {
                containingText.setTextContent(I18nHelper.getConstants().containing1Unit());
            } else {
                containingText.setTextContent(I18nHelper.getConstants().containingXUnits(count));
            }
            unloadModeButton.setEnabled(true);
        } else {
            containingText.setTextContent(I18nHelper.getConstants().containingNoUnits());
            unloadModeButton.setEnabled(false);
        }
    }

    @EventHandler("unloadModeButton")
    private void unloadModeButtonClick(ClickEvent event) {
        onUnloadPressed();
    }

    @Override
    public HTMLElement getElement() {
        return div;
    }
}
