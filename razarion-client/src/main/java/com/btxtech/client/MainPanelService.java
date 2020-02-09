package com.btxtech.client;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MainPanelService {
    @Inject
    private MainPanel mainPanel;

    public void init() {
        DomGlobal.document.body.appendChild(mainPanel.getElement());
    }

    public void addToGamePanel(IsElement isElement) {
        addToGamePanel(isElement.getElement());
    }

    public void addToGamePanel(HTMLElement htmlElement) {
        mainPanel.addToGamePanel(htmlElement);
    }

    public void removeFromGamePanel(IsElement isElement) {
        removeFromGamePanel(isElement.getElement());
    }

    public void removeFromGamePanel(HTMLElement htmlElement) {
        mainPanel.removeFromGamePanel(htmlElement);
    }

    public HTMLElement getMainPanelElement() {
        return mainPanel.getElement();
    }

    public void createLeftPanel(IsElement isElement) {
        throw new UnsupportedOperationException("MainPanelService.createLeftPanel() TODO");

    }
}
