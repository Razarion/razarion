package com.btxtech.client;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Logger;

@Templated("MainPanel.html#flexContainer")
public class MainPanel implements IsElement {
    private Logger logger = Logger.getLogger(MainPanel.class.getName());
    @Inject
    @DataField
    private HTMLDivElement flexContainer;
    @Inject
    @DataField
    private HTMLDivElement gamePanel;

    public void addToGamePanel(HTMLElement htmlElement) {
        gamePanel.appendChild(htmlElement);
    }

    public void removeFromGamePanel(HTMLElement htmlElement) {
        gamePanel.removeChild(htmlElement);
    }

    public void addToFlexContainer(HTMLElement htmlElement) {
        flexContainer.appendChild(htmlElement);
    }

    public void removeFromFlexContainer(HTMLElement htmlElement) {
        flexContainer.removeChild(htmlElement);
    }

    @Override
    public HTMLElement getElement() {
        return flexContainer;
    }
}
