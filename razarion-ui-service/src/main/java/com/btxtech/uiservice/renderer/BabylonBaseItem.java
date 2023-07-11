package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.nativejs.NativeVertexDto;
import jsinterop.annotations.JsType;


@JsType(isNative = true)
public interface BabylonBaseItem extends BabylonItem {
    double getHealth();

    void setHealth(double health);

    void updateHealth();

    void setBuildingPosition(NativeVertexDto buildingPosition);

    void setHarvestingPosition(NativeVertexDto harvestingPosition);

    void setBuildup(double buildup);

    void onProjectileFired(Vertex destination);

    void onExplode();
}
