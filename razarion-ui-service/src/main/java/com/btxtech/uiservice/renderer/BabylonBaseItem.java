package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.nativejs.NativeVertexDto;
import jsinterop.annotations.JsType;


@JsType(isNative = true)
public interface BabylonBaseItem {
    int getId();

    void dispose();

    Vertex getPosition();

    void setPosition(Vertex position);

    void updatePosition();

    double getAngle();

    void setAngle(double angle);

    void updateAngle();

    double getHealth();

    void setHealth(double health);

    void updateHealth();

    void select(boolean active);

    void hover(boolean active);

    void setBuildingPosition(NativeVertexDto buildingPosition);
}
