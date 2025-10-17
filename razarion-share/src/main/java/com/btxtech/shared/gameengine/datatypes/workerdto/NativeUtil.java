package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGroundSlopeBox;

import java.util.Arrays;

public interface NativeUtil {

    static DecimalPosition toSyncBaseItemPosition2d(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.contained) {
            return null;
        } else {
            return new DecimalPosition(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y);
        }
    }

    static Vertex toSyncBaseItemPosition3d(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.contained) {
            return null;
        } else {
            return new Vertex(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y, nativeSyncBaseItemTickInfo.z);
        }
    }

    static NativeDecimalPosition[] toNativeDecimalPositions(DecimalPosition[] decimalPositions) {
        if (decimalPositions != null) {
            return Arrays.stream(decimalPositions).map(decimalPosition -> {
                NativeDecimalPosition nativeDecimalPosition = new NativeDecimalPosition();
                nativeDecimalPosition.x = decimalPosition.getX();
                nativeDecimalPosition.y = decimalPosition.getY();
                return nativeDecimalPosition;
            }).toArray(NativeDecimalPosition[]::new);
        } else {
            return null;
        }
    }

    static NativeBotGroundSlopeBox[] toNativeBotGroundSlopeBoxes(BotGroundSlopeBox[] botGroundSlopeBoxes) {
        if (botGroundSlopeBoxes != null) {
            return Arrays.stream(botGroundSlopeBoxes).map(botGroundSlopeBox -> {
                NativeBotGroundSlopeBox nativeBotGroundSlopeBox = new NativeBotGroundSlopeBox();
                nativeBotGroundSlopeBox.xPos = botGroundSlopeBox.xPos;
                nativeBotGroundSlopeBox.yPos = botGroundSlopeBox.yPos;
                nativeBotGroundSlopeBox.height = botGroundSlopeBox.height;
                nativeBotGroundSlopeBox.yRot = botGroundSlopeBox.yRot;
                nativeBotGroundSlopeBox.zRot = botGroundSlopeBox.zRot;
                return nativeBotGroundSlopeBox;
            }).toArray(NativeBotGroundSlopeBox[]::new);
        } else {
            return null;
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

    static DecimalPosition[] toDecimalPositions(NativeDecimalPosition[] nativeDecimalPositions) {
        if (nativeDecimalPositions != null) {
            return Arrays.stream(nativeDecimalPositions).map(nativeDecimalPosition -> new DecimalPosition(nativeDecimalPosition.x, nativeDecimalPosition.y)).toArray(DecimalPosition[]::new);
        } else {
            return null;
        }
    }


    static BotGroundSlopeBox[] toBotGroundSlopeBoxes(NativeBotGroundSlopeBox[] nativeBotGroundSlopeBoxes) {
        if (nativeBotGroundSlopeBoxes != null) {
            return Arrays.stream(nativeBotGroundSlopeBoxes).map(nativeBotGroundSlopeBox -> {
                BotGroundSlopeBox botGroundSlopeBox = new BotGroundSlopeBox();
                botGroundSlopeBox.xPos = nativeBotGroundSlopeBox.xPos;
                botGroundSlopeBox.yPos = nativeBotGroundSlopeBox.yPos;
                botGroundSlopeBox.height = nativeBotGroundSlopeBox.height;
                botGroundSlopeBox.yRot = nativeBotGroundSlopeBox.yRot;
                botGroundSlopeBox.zRot = nativeBotGroundSlopeBox.zRot;
                return botGroundSlopeBox;
            }).toArray(BotGroundSlopeBox[]::new);
        } else {
            return null;
        }
    }
}
