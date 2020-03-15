package com.btxtech.client.cockpit;

import com.btxtech.client.MainPanelService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupSeq;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ProgressElement;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 10.07.2016.
 */
@Singleton
public class ClientScreenCoverImpl implements ScreenCover, StartupProgressListener {
    private static final String LOADING_COVER_ID = "RAZARION_LOADING_COVER";
    private static final String LOADING_PROGRESS_ID = "RAZARION_LOADING_PROGRESS";
    // private Logger logger = Logger.getLogger(ClientScreenCoverImpl.class.getName());
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private Instance<StoryCoverPanel> storyCoverPanelInstance;
    @Inject
    private Instance<EmptyCover> emptyCoverInstance;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private StoryCoverPanel storyCoverPanel;
    private int totalStartupTasks;
    private int finishedStartupTasks;
    private Element loadingCoverBackup;

    @Override
    public void showStoryCover(String text) {
        if (storyCoverPanel == null) {
            storyCoverPanel = storyCoverPanelInstance.get();
            storyCoverPanel.setText(text);
            storyCoverPanel.getElement().style.zIndex = ZIndexConstants.STORY_COVER;
            mainPanelService.addToGamePanel(storyCoverPanel);
        }
    }

    @Override
    public void hideStoryCover() {
        if (storyCoverPanel != null) {
            mainPanelService.removeFromGamePanel(storyCoverPanel);
            storyCoverPanel = null;
        }
    }

    @Override
    public void removeLoadingCover() {
        loadingCoverBackup = Browser.getDocument().getElementById(LOADING_COVER_ID);
        loadingCoverBackup.getParentElement().removeChild(loadingCoverBackup);
    }

    @Override
    public void fadeOutLoadingCover() {
        Element element = Browser.getDocument().getElementById(LOADING_COVER_ID);
        element.getStyle().setOpacity(0);
    }

    @Override
    public void fadeInLoadingCover() {
        Element element = Browser.getDocument().getElementById(LOADING_COVER_ID);
        if (element == null) {
            element = loadingCoverBackup;
            Browser.getDocument().getBody().appendChild(element);
            loadingCoverBackup = null;
        }
        element.getStyle().setOpacity(1);
    }

    @Override
    public void fadeOutAndForward(String url) {
        EmptyCover emptyCover = emptyCoverInstance.get();
        mainPanelService.addToGamePanel(emptyCover);
        emptyCover.startFadeout();
        simpleExecutorService.schedule(FADE_DURATION, () -> Browser.getWindow().getLocation().setHref(url), SimpleExecutorService.Type.COVER_FADE);
    }

    @Override
    public void onStart(StartupSeq startupSeq) {
        finishedStartupTasks = 0;
        totalStartupTasks = startupSeq.getAbstractStartupTaskEnum().length;
    }

    @Override
    public void onTaskFinished(AbstractStartupTask task) {
        try {
            finishedStartupTasks++;
            double progress = (double) finishedStartupTasks / (double) totalStartupTasks;
            ((ProgressElement) Browser.getDocument().getElementById(LOADING_PROGRESS_ID)).setValue(progress);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Override
    public void onFallback(Alarm.Type alarmType) {
        removeLoadingCover();
    }
}
