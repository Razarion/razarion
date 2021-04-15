package com.btxtech.client.playback;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.control.PlaybackControl;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.base.Js;
import org.jboss.errai.common.client.dom.Window;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static elemental2.dom.CSSProperties.HeightUnionType;
import static elemental2.dom.CSSProperties.WidthUnionType;
import static elemental2.dom.CanvasRenderingContext2D.FillStyleUnionType;
import static elemental2.dom.CanvasRenderingContext2D.StrokeStyleUnionType;

/**
 * Created by Beat
 * on 31.05.2017.
 */
@ApplicationScoped
public class ClientPlaybackControl extends PlaybackControl {
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private MainCockpitService cockpitService;
    private CanvasRenderingContext2D mousePlaybackContext;
    private Index lastMousePosition;
    private HTMLCanvasElement canvasElementPlaybackCanvas;
    @Inject
    private Instance<PlaybackSidebar> playbackSidebarInstance;
    @Inject
    private Instance<PlaybackDialog> playbackDialogInstance;
    private PlaybackSidebar playbackSidebar;
    private Date endTimeStamp;
    private Map<Integer, PlaybackDialog> openPlaybackDialogs = new HashMap<>();

    @Override
    protected void activatePlaybackMode(Date startTimeStamp, DetailedTracking endDetailedTracking) {
        gameCanvas.enterPlaybackMode();
        showMousePlaybackPanel();
        cockpitService.hide();
        endTimeStamp = endDetailedTracking.getTimeStamp();
        playbackSidebar = playbackSidebarInstance.get();
        mainPanelService.addPlaybackPanel(playbackSidebar);
        playbackSidebar.setPlaybackControl(this);
        playbackSidebar.displayRemainingTime(endTimeStamp.getTime() - startTimeStamp.getTime());
    }

    private void showMousePlaybackPanel() {
        canvasElementPlaybackCanvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
        if (canvasElementPlaybackCanvas == null) {
            throw new IllegalStateException("Canvas is not supported");
        }
        canvasElementPlaybackCanvas.style.zIndex = ZIndexConstants.PLAYBACK_MOUSE_CANVAS;
        canvasElementPlaybackCanvas.style.position = "absolute";

        mousePlaybackContext = Js.cast(canvasElementPlaybackCanvas.getContext("2d"));
        mainPanelService.addToGamePanel(canvasElementPlaybackCanvas);
    }

    @Override
    protected void setCanvasPlaybackDimension(Index browserWindowDimension) {
        try {
            gameCanvas.setPlaybackDimension(browserWindowDimension);
            canvasElementPlaybackCanvas.style.width = WidthUnionType.of(browserWindowDimension.getX() + "px");
            canvasElementPlaybackCanvas.style.height = HeightUnionType.of(browserWindowDimension.getY() + "px");
            canvasElementPlaybackCanvas.width = browserWindowDimension.getX();
            canvasElementPlaybackCanvas.height = browserWindowDimension.getY();
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    @Override
    protected void displayMouseMove(Index position) {
        if (lastMousePosition == null) {
            lastMousePosition = position;
            return;
        }
        mousePlaybackContext.beginPath();
        mousePlaybackContext.moveTo(lastMousePosition.getX(), lastMousePosition.getY());
        mousePlaybackContext.lineTo(position.getX(), position.getY());
        mousePlaybackContext.lineWidth = 2;
        mousePlaybackContext.strokeStyle = StrokeStyleUnionType.of("black");
        mousePlaybackContext.stroke();
        lastMousePosition = position;
    }

    @Override
    protected void displayMouseButton(int button, boolean down) {
        if (lastMousePosition == null) {
            return;
        }
        mousePlaybackContext.beginPath();
        mousePlaybackContext.arc(lastMousePosition.getX(), lastMousePosition.getY(), 5, 0, 2 * Math.PI, false);
        if (button == 0) {
            mousePlaybackContext.fillStyle = FillStyleUnionType.of("red");
        } else if (button == 1) {
            mousePlaybackContext.fillStyle = FillStyleUnionType.of("green");
        } else if (button == 2) {
            mousePlaybackContext.fillStyle = FillStyleUnionType.of("blue");
        } else {
            mousePlaybackContext.fillStyle = FillStyleUnionType.of("yello");
        }
        mousePlaybackContext.fill();
        mousePlaybackContext.lineWidth = 2;
        if (down) {
            mousePlaybackContext.strokeStyle = StrokeStyleUnionType.of("black");
        } else {
            mousePlaybackContext.strokeStyle = StrokeStyleUnionType.of("white");
        }
        mousePlaybackContext.stroke();
    }

    @Override
    protected void onNextAction(DetailedTracking nextDetailedTracking) {
        playbackSidebar.displayRemainingTime(endTimeStamp.getTime() - nextDetailedTracking.getTimeStamp().getTime());
    }

    @Override
    protected void onSleeping(long timeToSleep) {
        playbackSidebar.onOnSleeping(timeToSleep);
    }

    @Override
    protected void onOnPause() {
        playbackSidebar.onOnPause();
    }

    @Override
    protected void onFinished() {
        playbackSidebar.onFinished();
    }

    @Override
    protected void showPlaybackDialog(int identityHashCode, String title, int left, int top, int width, int height, int zIndex) {
        PlaybackDialog playbackDialog = playbackDialogInstance.get();
        playbackDialog.init(title, left, top, width, height, zIndex);
        openPlaybackDialogs.put(identityHashCode, playbackDialog);
        Window.getDocument().getBody().appendChild(playbackDialog.getElement());
    }

    @Override
    protected void hidePlaybackDialog(int identityHashCode) {
        PlaybackDialog playbackDialog = openPlaybackDialogs.remove(identityHashCode);
        Window.getDocument().getBody().removeChild(playbackDialog.getElement());
    }
}
