package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.projectile.ProjectileUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_BASE_ITEM;

/**
 * Created by Beat
 * 31.08.2016.
 */
@ApplicationScoped
@Deprecated
public class ProjectileRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private AlarmService alarmService;
    @Inject
    private ProjectileUiService projectileUiService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes()
                .stream()
                .filter(baseItemType -> baseItemType.getWeaponType() != null)
                .forEach(this::setupBaseItemType);
    }

    @Override
    protected void preRender(long timeStamp) {
        projectileUiService.preRender(timeStamp);
    }

    private void setupBaseItemType(BaseItemType baseItemType) {
        if (baseItemType.getWeaponType().getProjectileShape3DId() != null) {
            createShape3DRenderTasks(shape3DUiService.getShape3D(baseItemType.getWeaponType().getProjectileShape3DId()),
                    timeStamp -> projectileUiService.getProjectiles(baseItemType));
        } else {
            alarmService.riseAlarm(INVALID_BASE_ITEM, "WeaponType has no projectileShape3DId", baseItemType.getId());
        }
    }
}
