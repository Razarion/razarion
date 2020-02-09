package com.btxtech.client.playback;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.common.DisplayUtils;
import com.btxtech.uiservice.control.PlaybackControl;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Templated("PlaybackSidebar.html#playbackSidebar")
public class PlaybackSidebar implements IsElement {
    @Inject
    @DataField
    private HTMLDivElement playbackSidebar;
    @Inject
    @DataField
    private Label remainingTime;
    @Inject
    @DataField
    private Label stateLabel;
    @Inject
    @DataField
    private Span sleepingLabel;
    @Inject
    @DataField
    private Button pauseButton;
    @Inject
    @DataField
    private Button skipButton;
    private PlaybackControl playbackControl;

    @PostConstruct
    public void init() {
        playbackSidebar.style.zIndex = ZIndexConstants.PLAYBACK_SIDE_BAR;
        GwtUtils.preventContextMenu(playbackSidebar);
    }

    public void setPlaybackControl(PlaybackControl playbackControl) {
        this.playbackControl = playbackControl;
    }

    public void displayRemainingTime(long time) {
        remainingTime.setText(DisplayUtils.formatHourTimeStamp(time));
        stateLabel.setText("Playing");
    }

    public void onOnPause() {
        stateLabel.setText("Pause");
    }

    public void onFinished() {
        stateLabel.setText("Finished");
        sleepingLabel.setInnerHTML("");
    }

    public void onOnSleeping(long timeToSleep) {
        sleepingLabel.setInnerHTML(DisplayUtils.formatMinuteTimeStamp(timeToSleep));
    }

    @EventHandler("pauseButton")
    private void onPauseButtonClick(ClickEvent event) {
        playbackControl.pause();
    }

    @EventHandler("skipButton")
    private void onSkipButtonClick(ClickEvent event) {
        playbackControl.skip();
    }

    @Override
    public HTMLElement getElement() {
        return playbackSidebar;
    }
}
