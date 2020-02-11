package com.btxtech.client;

import com.btxtech.client.editor.sidebar.SideBarPanel;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MainPanelService {
    @Inject
    private MainPanel mainPanel;
    @Inject
    private ExceptionHandler exceptionHandler;

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

    public void addEditorPanel(SideBarPanel sideBarPanel) {
        mainPanel.addToFlexContainer(sideBarPanel.getElement());
    }

    public void removeEditorPanel(SideBarPanel sideBarPanel) {
        mainPanel.removeFromFlexContainer(sideBarPanel.getElement());
    }

    public void addPlaybackPanel(IsElement isElement) {
        throw new UnsupportedOperationException("MainPanelService.addPlaybackPanel() TODO");

    }

    public HTMLElement getMainPanelElement() {
        return mainPanel.getElement();
    }
}
