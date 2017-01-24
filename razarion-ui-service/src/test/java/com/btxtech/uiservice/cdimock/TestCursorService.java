package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.mouse.CursorService;
import com.btxtech.uiservice.mouse.CursorType;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestCursorService extends CursorService {
    @Override
    protected void setCursorInternal(CursorType cursorType, boolean allowed) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setDefaultCursorInternal() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setPointerCursorInternal() {
        throw new UnsupportedOperationException();
    }
}
