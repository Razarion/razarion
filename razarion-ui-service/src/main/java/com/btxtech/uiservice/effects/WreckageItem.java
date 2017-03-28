package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

/**
 * Created by Beat
 * 10.02.2017.
 */
public class WreckageItem {
    private BaseItemType baseItemType;
    private long visibleTillTimeStamp;
    private ModelMatrices modelMatrices;

    public WreckageItem(BaseItemType baseItemType, Vertex position, NativeMatrixFactory nativeMatrixFactory) {
        visibleTillTimeStamp = System.currentTimeMillis() + TrailService.VISIBLE_WRECKAGE_MILLIS;
        this.baseItemType = baseItemType;
        modelMatrices = ModelMatrices.create4Wreckage(position, MathHelper.getRandomAngle(), nativeMatrixFactory);
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
