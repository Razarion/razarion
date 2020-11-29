package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.renderer.WebGlRenderTask;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_BASE_ITEM;

/**
 * Created by Beat
 * 05.09.2016.
 */
@ApplicationScoped
public class BaseItemPlacerRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    public interface Circle extends WebGlRenderTask<BaseItemPlacer> {
    }

    // private Logger logger = Logger.getLogger(BaseItemPlacerRenderTaskRunner.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private AlarmService alarmService;

    public void activate(BaseItemPlacer baseItemPlacer) {
        Circle circle = createModelRenderTask(BaseItemPlacerRenderTaskRunner.Circle.class, baseItemPlacer, timeStamp -> baseItemPlacer.provideItemModelMatrices(), null, null, null);
        circle.setActive(true);
        if (baseItemPlacer.getBaseItemType().getShape3DId() == null) {
            alarmService.riseAlarm(INVALID_BASE_ITEM, "No shape3DId for BaseItemType", baseItemPlacer.getBaseItemType().getId());
        }
        createShape3DRenderTasks(shape3DUiService.getShape3D(baseItemPlacer.getBaseItemType().getShape3DId()), timeStamp -> baseItemPlacer.provideItemModelMatrices());
    }

    public void deactivate() {
        destroyRenderAllTasks();
    }
}
