package com.btxtech.uiservice.cdimock.renderer;

import com.btxtech.uiservice.renderer.task.selection.ItemMarkerGeometry;
import com.btxtech.uiservice.renderer.task.selection.ItemMarkerRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.selection.StatusBarGeometry;
import com.btxtech.uiservice.renderer.task.selection.StatusBarRenderTaskRunner;

public class StatusBarRenderTaskMock extends AbstractRenderTaskMock<ItemMarkerGeometry> implements StatusBarRenderTaskRunner.RenderTask {
    @Override
    public void init(StatusBarGeometry statusBarGeometry) {

    }
}
