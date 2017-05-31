package com.btxtech.client;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.PlaybackControl;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 31.05.2017.
 */
@ApplicationScoped
public class ClientPlaybackControl extends PlaybackControl {
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Context2d mousePlaybackContext;
    private Index lastMousePosition;
    private Canvas mousePlaybackCanvas;

    @Override
    protected void activatePlaybackMode() {
        gameCanvas.enterPlaybackMode();
        showMousePlaybackPanel();
    }

    private void showMousePlaybackPanel() {
        mousePlaybackCanvas = Canvas.createIfSupported();
        if (mousePlaybackCanvas == null) {
            throw new IllegalStateException("Canvas is not supported");
        }
        mousePlaybackCanvas.getElement().getStyle().setZIndex(ZIndexConstants.PLAYBACK_MOUSE_CANVAS);
        mousePlaybackCanvas.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        mousePlaybackContext = (Context2d) mousePlaybackCanvas.getContext("2d");
        RootPanel.get().add(mousePlaybackCanvas);
    }

    @Override
    protected void setCanvasPlaybackDimension(Index browserWindowDimension) {
        try {
            gameCanvas.setPlaybackDimension(browserWindowDimension);
            mousePlaybackCanvas.getElement().getStyle().setWidth(browserWindowDimension.getX(), Style.Unit.PX);
            mousePlaybackCanvas.getElement().getStyle().setHeight(browserWindowDimension.getY(), Style.Unit.PX);
            mousePlaybackCanvas.setCoordinateSpaceWidth(browserWindowDimension.getX());
            mousePlaybackCanvas.setCoordinateSpaceHeight(browserWindowDimension.getY());
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
        mousePlaybackContext.setLineWidth(2);
        mousePlaybackContext.setStrokeStyle("black");
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
            mousePlaybackContext.setFillStyle("red");
        } else if (button == 1) {
            mousePlaybackContext.setFillStyle("green");
        } else if (button == 2) {
            mousePlaybackContext.setFillStyle("blue");
        } else {
            mousePlaybackContext.setFillStyle("yello");
        }
        mousePlaybackContext.fill();
        mousePlaybackContext.setLineWidth(2);
        if (down) {
            mousePlaybackContext.setStrokeStyle("black");
        } else {
            mousePlaybackContext.setStrokeStyle("white");
        }
        mousePlaybackContext.stroke();
    }
}
