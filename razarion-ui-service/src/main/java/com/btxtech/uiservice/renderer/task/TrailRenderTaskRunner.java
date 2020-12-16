package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.effects.TrailService;
import com.btxtech.uiservice.item.BaseItemUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 10.02.2017.
 */
@ApplicationScoped
public class TrailRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    // private Logger logger = Logger.getLogger(TrailRenderTask.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private TrailService trailService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().forEach(this::setupWreckage);
    }

    private void setupWreckage(BaseItemType baseItemType) {
        if (baseItemType.getWreckageShape3DId() != null) {
            createShape3DRenderTasks(shape3DUiService.getShape3D(baseItemType.getWreckageShape3DId()),
                    timeStamp -> trailService.provideWreckageModelMatrices(baseItemType));
        }
    }
}
