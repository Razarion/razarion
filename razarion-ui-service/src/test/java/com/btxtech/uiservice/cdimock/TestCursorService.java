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
        System.out.println("TestCursorService.setCursorInternal(): " + cursorType + " " + allowed);
    }

    @Override
    protected void setDefaultCursorInternal() {
        System.out.println("TestCursorService.setDefaultCursorInternal()");
    }

    @Override
    protected void setPointerCursorInternal() {
        System.out.println("TestCursorService.setPointerCursorInternal()");
    }
}
