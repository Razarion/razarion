package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import jsinterop.annotations.JsType;


@JsType(isNative = true)
public interface BabylonBaseItem extends BabylonItem {
    BaseItemType getBaseItemType();

    boolean isEnemy();

    void setHealth(double health);

    void setBuildingPosition(DecimalPosition buildingPosition);

    void setHarvestingPosition(DecimalPosition harvestingPosition);

    void setBuildup(double buildup);

    void setConstructing(double progress);

    void setIdle(boolean idle);

    void onProjectileFired(int tagetSyncBaseItemId, DecimalPosition targetPosition);

    void onExplode();

    void updateUserName(String userName);

    int getBaseId();
}
