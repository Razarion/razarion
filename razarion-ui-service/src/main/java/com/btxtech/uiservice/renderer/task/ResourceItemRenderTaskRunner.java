package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.item.ResourceUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_RESOURCE_ITEM;

/**
 * Created by Beat
 * 31.08.2016.
 */
@ApplicationScoped
public class ResourceItemRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    private Logger logger = Logger.getLogger(ResourceItemRenderTaskRunner.class.getName());
    @Inject
    private AlarmService alarmService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ResourceUiService resourceUiService;

    @PostConstruct
    public void postConstruct() {
        itemTypeService.getResourceItemTypes().forEach(this::setupResourceItemType);
    }

    private void setupResourceItemType(ResourceItemType resourceItemType) {
//        if (resourceItemType.getShape3DId() != null) {
//            createShape3DRenderTasks(shape3DUiService.getShape3D(resourceItemType.getShape3DId()),
//                    timeStamp -> resourceUiService.provideModelMatrices(resourceItemType),
//                    null,
//                    null);
//        } else {
//            alarmService.riseAlarm(INVALID_RESOURCE_ITEM, "No shape3DId", resourceItemType.getId());
//        }
    }

}
