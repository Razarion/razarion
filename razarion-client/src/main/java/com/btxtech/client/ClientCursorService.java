package com.btxtech.client;

import com.btxtech.client.gwtangular.AngularCursorService;
import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.CursorType;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 30.11.2016.
 */
@ApplicationScoped
public class ClientCursorService extends CursorService {
    private final Logger logger = Logger.getLogger(ClientCursorService.class.getName());

    private AngularCursorService angularCursorService;

    public void init(AngularCursorService angularCursorService) {
        this.angularCursorService = angularCursorService;
    }

    @Override
    protected void setDefaultCursorInternal() {
        if (this.angularCursorService != null) {
            this.angularCursorService.setDefaultCursor();
        } else {
            logger.warning("No angularCursorService setDefaultCursor()");
        }
    }

    @Override
    protected void setPointerCursorInternal() {
        if (this.angularCursorService != null) {
            this.angularCursorService.setPointerCursor();
        } else {
            logger.warning("No angularCursorService setPointerCursor()");
        }
    }

    @Override
    protected void setCursorInternal(CursorType cursorType, boolean allowed) {
        if (this.angularCursorService != null) {
            this.angularCursorService.setCursor(cursorType, allowed);
        } else {
            logger.warning("No angularCursorService setCursor(" + cursorType + ", " + allowed + ")");
        }
    }
}
