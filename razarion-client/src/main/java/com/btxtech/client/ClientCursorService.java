package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.CursorType;
import elemental.css.CSSStyleDeclaration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 30.11.2016.
 */
@ApplicationScoped
public class ClientCursorService extends CursorService {
    @Inject
    private GameCanvas gameCanvas;

    @Override
    protected void setDefaultCursorInternal() {
        gameCanvas.getCanvasElement().getStyle().setCursor(CSSStyleDeclaration.Cursor.DEFAULT);
    }

    @Override
    protected void setPointerCursorInternal() {
        gameCanvas.getCanvasElement().getStyle().setCursor(CSSStyleDeclaration.Cursor.POINTER);
    }

    @Override
    protected void setCursorInternal(CursorType cursorType, boolean allowed) {
        gameCanvas.getCanvasElement().getStyle().setCursor("url(" + StaticResourcePath.getCursorPath(cursorType.getName(allowed)) + ") " + cursorType.getHotSpotX(allowed) + " " + cursorType.getHotSpotY(allowed) + ", " + cursorType.getAlternativeDefault(allowed));
    }
}
