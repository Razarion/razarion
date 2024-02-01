package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.nativejs.NativeVertexDto;
import jsinterop.annotations.JsType;


@JsType(isNative = true)
public interface BabylonBaseItem extends BabylonItem {
    BaseItemType getBaseItemType();

    boolean isEnemy();

    void setHealth(double health);

    void setBuildingPosition(NativeVertexDto buildingPosition);

    void setHarvestingPosition(NativeVertexDto harvestingPosition);

    void setBuildup(double buildup);

    void setConstructing(double progress);

    void onProjectileFired(Vertex destination);

    void onExplode();
}
