package com.btxtech.client.cockpit;

import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.google.gwt.user.client.ui.RootPanel;
import elemental.client.Browser;
import elemental.dom.Element;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 10.07.2016.
 */
@Singleton
public class ClientScreenCoverImpl implements ScreenCover {
    private static final String LOADING_COVER_ID = "RAZARION_LOADING_COVER";
    // private Logger logger = Logger.getLogger(ClientScreenCoverImpl.class.getName());
    @Inject
    private Instance<StoryCoverPanel> storyCoverPanelInstance;
    @Inject
    private Instance<EmptyCover> emptyCoverInstance;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private StoryCoverPanel storyCoverPanel;

    @Override
    public void showStoryCover(String text) {
        if (storyCoverPanel == null) {
            storyCoverPanel = storyCoverPanelInstance.get();
            storyCoverPanel.setText(text);
            storyCoverPanel.getElement().getStyle().setZIndex(ZIndexConstants.STORY_COVER);
            RootPanel.get().add(storyCoverPanel);
        }
    }

    @Override
    public void hideStoryCover() {
        if (storyCoverPanel != null) {
            RootPanel.get().remove(storyCoverPanel);
            storyCoverPanel = null;
        }
    }

    @Override
    public void removeLoadingCover() {
        Browser.getDocument().getBody().removeChild(Browser.getDocument().getElementById(LOADING_COVER_ID));
    }

    @Override
    public void fadeOutLoadingCover() {
        Element element = Browser.getDocument().getElementById(LOADING_COVER_ID);
        element.getStyle().setOpacity(0);
    }

    @Override
    public void fadeOutAndForward(String url) {
        EmptyCover emptyCover = emptyCoverInstance.get();
        RootPanel.get().add(emptyCover);
        emptyCover.startFadeout();
        simpleExecutorService.schedule(FADE_DURATION, () -> Browser.getWindow().getLocation().setHref(url), SimpleExecutorService.Type.UNSPECIFIED);
    }
}
