package com.btxtech.uiservice.cdimock.renderer;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RenderTaskCollector {
    private List<AbstractRenderTaskMock<?>> abstractRenderTaskMocks = new ArrayList<>();

    public void addRendererTask(AbstractRenderTaskMock<?> abstractRenderTaskMock) {
        abstractRenderTaskMocks.add(abstractRenderTaskMock);
    }

    public List<AbstractRenderTaskMock<?>> getAbstractRenderTaskMocks() {
        return abstractRenderTaskMocks;
    }
}
