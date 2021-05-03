package com.btxtech.client;

import com.btxtech.client.editor.editorpanel.EditorPanel;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Deprecated
public class MainPanelService {
    @Inject
    private ExceptionHandler exceptionHandler;
    private List<Runnable> resizeListeners = new ArrayList<>();

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

    public void addEditorPanel(EditorPanel editorPanel) {
        // TODO mainPanel.addToFlexContainer(editorPanel.getElement());
    }

    public void removeEditorPanel(EditorPanel editorPanel) {
        // TODO mainPanel.removeFromFlexContainer(editorPanel.getElement());
    }

    public void addPlaybackPanel(IsElement isElement) {
        throw new UnsupportedOperationException("MainPanelService.addPlaybackPanel() TODO");
    }

    public HTMLElement getMainPanelElement() {
        // TODO return mainPanel.getElement();
        throw new UnsupportedOperationException("... TODO ...");
    }
}
