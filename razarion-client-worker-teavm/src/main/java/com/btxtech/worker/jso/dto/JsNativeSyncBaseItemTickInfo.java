package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeSyncBaseItemTickInfo
 * Contains tick update data for base items
 */
public interface JsNativeSyncBaseItemTickInfo extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeSyncBaseItemTickInfo create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    int getId();

    @JSProperty
    void setId(int id);

    @JSProperty
    int getItemTypeId();

    @JSProperty
    void setItemTypeId(int itemTypeId);

    @JSProperty
    double getX();

    @JSProperty
    void setX(double x);

    @JSProperty
    double getY();

    @JSProperty
    void setY(double y);

    @JSProperty
    double getZ();

    @JSProperty
    void setZ(double z);

    @JSProperty
    double getAngle();

    @JSProperty
    void setAngle(double angle);

    @JSProperty
    int getBaseId();

    @JSProperty
    void setBaseId(int baseId);

    @JSProperty
    double getTurretAngle();

    @JSProperty
    void setTurretAngle(double turretAngle);

    @JSProperty
    double getSpawning();

    @JSProperty
    void setSpawning(double spawning);

    @JSProperty
    double getBuildup();

    @JSProperty
    void setBuildup(double buildup);

    @JSProperty
    double getHealth();

    @JSProperty
    void setHealth(double health);

    @JSProperty
    double getConstructing();

    @JSProperty
    void setConstructing(double constructing);

    @JSProperty
    int getConstructingBaseItemTypeId();

    @JSProperty
    void setConstructingBaseItemTypeId(int typeId);

    @JSProperty
    JsNativeDecimalPosition getHarvestingResourcePosition();

    @JSProperty
    void setHarvestingResourcePosition(JsNativeDecimalPosition pos);

    @JSProperty
    JsNativeDecimalPosition getBuildingPosition();

    @JSProperty
    void setBuildingPosition(JsNativeDecimalPosition pos);

    @JSProperty
    int[] getContainingItemTypeIds();

    @JSProperty
    void setContainingItemTypeIds(int[] ids);

    @JSProperty
    double getMaxContainingRadius();

    @JSProperty
    void setMaxContainingRadius(double radius);

    @JSProperty
    boolean isContained();

    @JSProperty
    void setContained(boolean contained);

    @JSProperty
    boolean isIdle();

    @JSProperty
    void setIdle(boolean idle);
}
