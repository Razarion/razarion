package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * on 12.01.2018.
 */
public interface NativeUtil {

    static DecimalPosition toSyncBaseItemPosition2d(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.contained) {
            return null;
        } else {
            return new DecimalPosition(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y);
        }
    }

    static NativeDecimalPosition toNativeDecimalPosition(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            NativeDecimalPosition nativeDecimalPosition = new NativeDecimalPosition();
            nativeDecimalPosition.x = decimalPosition.getX();
            nativeDecimalPosition.y = decimalPosition.getY();
            return nativeDecimalPosition;
        } else {
            return null;
        }
    }

    static DecimalPosition toDecimalPosition(NativeDecimalPosition nativeDecimalPosition) {
        if (nativeDecimalPosition != null) {
            return new DecimalPosition(nativeDecimalPosition.x, nativeDecimalPosition.y);
        } else {
            return null;
        }
    }


}
