package com.btxtech.client;

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Deprecated
public class MainPanelService {
    public void init() {
        // TODO DomGlobal.document.body.appendChild(mainPanel.getElement());
    }

    public void addToGamePanel(IsElement isElement) {
        addToGamePanel(isElement.getElement());
    }

    public void addToGamePanel(HTMLElement htmlElement) {
        // TODO mainPanel.addToGamePanel(htmlElement);
    }

    public void removeFromGamePanel(IsElement isElement) {
        removeFromGamePanel(isElement.getElement());
    }

    public void removeFromGamePanel(HTMLElement htmlElement) {
        // TODO mainPanel.removeFromGamePanel(htmlElement);
    }

    public void addPlaybackPanel(IsElement isElement) {
        throw new UnsupportedOperationException("MainPanelService.addPlaybackPanel() TODO");
    }
}
