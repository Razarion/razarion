package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.CursorType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 30.11.2016.
 */
@ApplicationScoped
public class ClientCursorService extends CursorService {
    // private Logger logger = Logger.getLogger(ClientCursorService.class.getName());
    @Inject
    private GameCanvas gameCanvas;

    @Override
    protected void setDefaultCursorInternal() {
        gameCanvas.setCursor("default");
    }

    @Override
    protected void setPointerCursorInternal() {
        gameCanvas.setCursor("pointer");
    }

    @Override
    protected void setCursorInternal(CursorType cursorType, boolean allowed) {
        gameCanvas.setCursor("url('" + StaticResourcePath.getCursorPath(cursorType.getName(allowed)) + "') " + cursorType.getHotSpotX(allowed) + " " + cursorType.getHotSpotY(allowed) + ", " + cursorType.getAlternativeDefault(allowed));
    }
}
