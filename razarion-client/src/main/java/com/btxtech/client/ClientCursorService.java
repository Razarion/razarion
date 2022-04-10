package com.btxtech.client;

import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.CursorType;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 30.11.2016.
 */
@ApplicationScoped
public class ClientCursorService extends CursorService {
    // private Logger logger = Logger.getLogger(ClientCursorService.class.getName());

    @Override
    protected void setDefaultCursorInternal() {
        // TODO gameCanvas.setCursor("default");
    }

    @Override
    protected void setPointerCursorInternal() {
        // TODO gameCanvas.setCursor("pointer");
    }

    @Override
    protected void setCursorInternal(CursorType cursorType, boolean allowed) {
        // TODO gameCanvas.setCursor("url('" + StaticResourcePath.getCursorPath(cursorType.getName(allowed)) + "') " + cursorType.getHotSpotX(allowed) + " " + cursorType.getHotSpotY(allowed) + ", " + cursorType.getAlternativeDefault(allowed));
    }
}
