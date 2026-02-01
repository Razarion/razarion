package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGroundSlopeBox;

import java.util.Arrays;
import java.util.logging.Logger;

public interface NativeUtil {
    Logger LOGGER = Logger.getLogger("NativeUtil");

    /**
     * Check if a double value is invalid (NaN or Infinite).
     * Uses multiple methods for robustness across different runtimes (JVM, GWT, TeaVM WASM).
     * The x != x idiom is always true only for NaN and works reliably in all JavaScript runtimes.
     */
    static boolean isInvalidDouble(double value) {
        // x != x is true only for NaN (more reliable than Double.isNaN in some JS runtimes like TeaVM WASM)
        return value != value || Double.isNaN(value) || Double.isInfinite(value);
    }

    static DecimalPosition toSyncBaseItemPosition2d(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.contained) {
            return null;
        }
        double x = nativeSyncBaseItemTickInfo.x;
        double y = nativeSyncBaseItemTickInfo.y;
        // Use 0 as fallback for invalid values to keep units functional
        if (isInvalidDouble(x)) {
            LOGGER.warning("[NativeUtil] Invalid x=" + x + " for item " + nativeSyncBaseItemTickInfo.id + ", using 0");
            x = 0;
        }
        if (isInvalidDouble(y)) {
            LOGGER.warning("[NativeUtil] Invalid y=" + y + " for item " + nativeSyncBaseItemTickInfo.id + ", using 0");
            y = 0;
        }
        return new DecimalPosition(x, y);
    }

    static Vertex toSyncBaseItemPosition3d(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (nativeSyncBaseItemTickInfo.contained) {
            return null;
        }
        double x = nativeSyncBaseItemTickInfo.x;
        double y = nativeSyncBaseItemTickInfo.y;
        double z = nativeSyncBaseItemTickInfo.z;
        // Use 0 as fallback for invalid values to keep units functional
        if (isInvalidDouble(x)) {
            LOGGER.warning("[NativeUtil] Invalid x=" + x + " for item " + nativeSyncBaseItemTickInfo.id + ", using 0");
            x = 0;
        }
        if (isInvalidDouble(y)) {
            LOGGER.warning("[NativeUtil] Invalid y=" + y + " for item " + nativeSyncBaseItemTickInfo.id + ", using 0");
            y = 0;
        }
        if (isInvalidDouble(z)) {
            LOGGER.warning("[NativeUtil] Invalid z=" + z + " for item " + nativeSyncBaseItemTickInfo.id + ", using 0");
            z = 0;
        }
        return new Vertex(x, y, z);
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
            // Skip positions with invalid values
            if (isInvalidDouble(nativeDecimalPosition.x) || isInvalidDouble(nativeDecimalPosition.y)) {
                return null;
            }
            return new DecimalPosition(nativeDecimalPosition.x, nativeDecimalPosition.y);
        } else {
            return null;
        }
    }

    static DecimalPosition[] toDecimalPositions(NativeDecimalPosition[] nativeDecimalPositions) {
        if (nativeDecimalPositions != null) {
            return Arrays.stream(nativeDecimalPositions)
                .filter(nativeDecimalPosition -> nativeDecimalPosition != null &&
                    !isInvalidDouble(nativeDecimalPosition.x) && !isInvalidDouble(nativeDecimalPosition.y))
                .map(nativeDecimalPosition -> new DecimalPosition(nativeDecimalPosition.x, nativeDecimalPosition.y))
                .toArray(DecimalPosition[]::new);
        } else {
            return null;
        }
    }


    static BotGroundSlopeBox[] toBotGroundSlopeBoxes(NativeBotGroundSlopeBox[] nativeBotGroundSlopeBoxes) {
        if (nativeBotGroundSlopeBoxes != null) {
            return Arrays.stream(nativeBotGroundSlopeBoxes)
                .filter(nativeBotGroundSlopeBox -> nativeBotGroundSlopeBox != null &&
                    !isInvalidDouble(nativeBotGroundSlopeBox.xPos) && !isInvalidDouble(nativeBotGroundSlopeBox.yPos))
                .map(nativeBotGroundSlopeBox -> {
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
