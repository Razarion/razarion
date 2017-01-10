package com.btxtech.uiservice.projectile;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.clip.EffectService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 10.01.2017.
 */
@ApplicationScoped
public class ProjectileUiService {
    @Inject
    private EffectService effectService;
    @Inject
    private ItemTypeService itemTypeService;
    private final Collection<ProjectileUi> projectiles = new ArrayList<>();
    private MapList<BaseItemType, ModelMatrices> modelMatrices = new MapList<>();

    public void onProjectileFired(int baseItemTypeId, Vertex muzzlePosition, Vertex target) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        ProjectileUi projectileUi = new ProjectileUi(baseItemType, muzzlePosition, target, baseItemType.getWeaponType().getProjectileSpeed());
        synchronized (projectiles) {
            projectiles.add(projectileUi);
        }
        effectService.onProjectileFired(baseItemType, muzzlePosition, target);
    }

    public void preRender(long timeStamp) {
        MapList<BaseItemType, ModelMatrices> modelMatricesTmp = new MapList<>();
        synchronized (projectiles) {
            for (Iterator<ProjectileUi> iterator = projectiles.iterator(); iterator.hasNext(); ) {
                ProjectileUi projectile = iterator.next();
                projectile.setupDistance(timeStamp);
                if (projectile.destinationReached()) {
                    iterator.remove();
                } else {
                    modelMatricesTmp.put(projectile.getBaseItemType(), projectile.createInterpolatedModelMatrices());
                }
            }
        }
        modelMatrices = modelMatricesTmp;
    }

    public List<ModelMatrices> getProjectiles(BaseItemType baseItemType) {
        return modelMatrices.get(baseItemType);
    }


}
