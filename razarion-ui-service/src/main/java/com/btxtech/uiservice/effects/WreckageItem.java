package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 10.02.2017.
 */
public class WreckageItem {
    private BaseItemType baseItemType;
    private long visibleTillTimeStamp;
    private ModelMatrices modelMatrices;

    public WreckageItem(BaseItemType baseItemType, Vertex position) {
        visibleTillTimeStamp = System.currentTimeMillis() + TrailService.VISIBLE_WRECKAGE_MILLIS;
        this.baseItemType = baseItemType;
        modelMatrices = new ModelMatrices(Matrix4.createTranslation(position).multiply(Matrix4.createZRotation(MathHelper.getRandomAngle())));
    }

    public ModelMatrices getModelMatrices() {
        return modelMatrices;
    }

    public boolean isExpired() {
        return visibleTillTimeStamp < System.currentTimeMillis();
    }

    public BaseItemType getBaseItemType() {
        return baseItemType;
    }
}
