package com.btxtech.client.jso;

import com.btxtech.shared.gameengine.datatypes.workerdto.NativeDecimalPosition;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SharedTickBufferLayout;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * Client-side (main thread) reader for the SharedArrayBuffer double-buffer tick data.
 * Reads data written by SharedTickBufferWriter without any postMessage overhead.
 */
public class SharedTickBufferReader {
    private final JSObject sharedArrayBuffer;
    private final JSObject controlInt32;
    private int lastReadSeq = 0;

    public SharedTickBufferReader(JSObject sharedArrayBuffer) {
        this.sharedArrayBuffer = sharedArrayBuffer;
        this.controlInt32 = createInt32View(sharedArrayBuffer, 0, 4);
    }

    /**
     * Returns true if the worker has written new tick data since the last read.
     */
    public boolean hasNewData() {
        int writeSeq = atomicsLoad(controlInt32, SharedTickBufferLayout.CTRL_WRITE_SEQUENCE);
        return writeSeq > 0 && writeSeq != lastReadSeq;
    }

    /**
     * Reads the current tick data from the active read buffer.
     * Only call this when hasNewData() returns true.
     */
    public NativeTickInfo readTickData() {
        int writeSeq = atomicsLoad(controlInt32, SharedTickBufferLayout.CTRL_WRITE_SEQUENCE);
        int activeBuffer = atomicsLoad(controlInt32, SharedTickBufferLayout.CTRL_ACTIVE_BUFFER);
        int bufferByteOffset = SharedTickBufferLayout.bufferOffset(activeBuffer);

        // Read header
        JSObject headerView = createInt32View(sharedArrayBuffer, bufferByteOffset, SharedTickBufferLayout.HEADER_BYTES / 4);
        int itemCount = getInt32(headerView, 0);
        int killedCount = getInt32(headerView, 1);
        int removeCount = getInt32(headerView, 2);

        NativeTickInfo result = new NativeTickInfo();
        result.resources = getInt32(headerView, 3);
        result.xpFromKills = getInt32(headerView, 4);
        result.houseSpace = getInt32(headerView, 5);

        // Read updated items
        if (itemCount > 0) {
            JSObject itemDoubles = createFloat64View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.ITEMS_DOUBLES_OFFSET,
                    itemCount * SharedTickBufferLayout.DOUBLES_PER_ITEM);
            JSObject itemInts = createInt32View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.ITEMS_INTS_OFFSET,
                    itemCount * SharedTickBufferLayout.INTS_PER_ITEM);
            JSObject itemFlags = createUint8View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.ITEMS_FLAGS_OFFSET,
                    itemCount);
            JSObject containingView = createInt32View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.CONTAINING_OFFSET,
                    SharedTickBufferLayout.MAX_CONTAINING_INTS);

            result.updatedNativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[itemCount];
            int containingOffset = 0;

            for (int i = 0; i < itemCount; i++) {
                NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
                int dOff = i * SharedTickBufferLayout.DOUBLES_PER_ITEM;
                int iOff = i * SharedTickBufferLayout.INTS_PER_ITEM;

                // Doubles
                info.x = getFloat64(itemDoubles, dOff + 0);
                info.y = getFloat64(itemDoubles, dOff + 1);
                info.z = getFloat64(itemDoubles, dOff + 2);
                info.angle = getFloat64(itemDoubles, dOff + 3);
                info.turretAngle = getFloat64(itemDoubles, dOff + 4);
                info.spawning = getFloat64(itemDoubles, dOff + 5);
                info.buildup = getFloat64(itemDoubles, dOff + 6);
                info.health = getFloat64(itemDoubles, dOff + 7);
                info.constructing = getFloat64(itemDoubles, dOff + 8);
                info.maxContainingRadius = getFloat64(itemDoubles, dOff + 9);

                double harvestX = getFloat64(itemDoubles, dOff + 10);
                if (!Double.isNaN(harvestX)) {
                    info.harvestingResourcePosition = new NativeDecimalPosition();
                    info.harvestingResourcePosition.x = harvestX;
                    info.harvestingResourcePosition.y = getFloat64(itemDoubles, dOff + 11);
                }
                double buildX = getFloat64(itemDoubles, dOff + 12);
                if (!Double.isNaN(buildX)) {
                    info.buildingPosition = new NativeDecimalPosition();
                    info.buildingPosition.x = buildX;
                    info.buildingPosition.y = getFloat64(itemDoubles, dOff + 13);
                }

                // Ints
                info.id = getInt32(itemInts, iOff + 0);
                info.itemTypeId = getInt32(itemInts, iOff + 1);
                info.baseId = getInt32(itemInts, iOff + 2);
                info.constructingBaseItemTypeId = getInt32(itemInts, iOff + 3);

                // Flags
                int flags = getUint8(itemFlags, i);
                info.contained = (flags & 1) != 0;
                info.idle = (flags & 2) != 0;
                boolean hasContaining = (flags & 4) != 0;

                // ContainingItemTypeIds
                if (hasContaining) {
                    int count = getInt32(containingView, containingOffset++);
                    info.containingItemTypeIds = new int[count];
                    for (int c = 0; c < count; c++) {
                        info.containingItemTypeIds[c] = getInt32(containingView, containingOffset++);
                    }
                }

                result.updatedNativeSyncBaseItemTickInfos[i] = info;
            }
        }

        // Read killed items
        if (killedCount > 0) {
            JSObject killedDoubles = createFloat64View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.KILLED_DOUBLES_OFFSET,
                    killedCount * SharedTickBufferLayout.KILLED_DOUBLES_PER_ITEM);
            JSObject killedInts = createInt32View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.KILLED_INTS_OFFSET,
                    killedCount * SharedTickBufferLayout.KILLED_INTS_PER_ITEM);
            JSObject killedFlags = createUint8View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.KILLED_FLAGS_OFFSET,
                    killedCount);

            result.killedSyncBaseItems = new NativeSimpleSyncBaseItemTickInfo[killedCount];
            for (int i = 0; i < killedCount; i++) {
                NativeSimpleSyncBaseItemTickInfo k = new NativeSimpleSyncBaseItemTickInfo();
                k.x = getFloat64(killedDoubles, i * 2);
                k.y = getFloat64(killedDoubles, i * 2 + 1);
                k.id = getInt32(killedInts, i * 2);
                k.itemTypeId = getInt32(killedInts, i * 2 + 1);
                k.contained = getUint8(killedFlags, i) != 0;
                result.killedSyncBaseItems[i] = k;
            }
        }

        // Read remove IDs
        if (removeCount > 0) {
            JSObject removeView = createInt32View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.REMOVE_OFFSET,
                    removeCount);
            result.removeSyncBaseItemIds = new int[removeCount];
            for (int i = 0; i < removeCount; i++) {
                result.removeSyncBaseItemIds[i] = getInt32(removeView, i);
            }
        }

        lastReadSeq = writeSeq;
        return result;
    }

    // --- JS interop ---

    @JSBody(params = {"sab", "byteOffset", "length"}, script = "return new Int32Array(sab, byteOffset, length);")
    private static native JSObject createInt32View(JSObject sab, int byteOffset, int length);

    @JSBody(params = {"sab", "byteOffset", "length"}, script = "return new Float64Array(sab, byteOffset, length);")
    private static native JSObject createFloat64View(JSObject sab, int byteOffset, int length);

    @JSBody(params = {"sab", "byteOffset", "length"}, script = "return new Uint8Array(sab, byteOffset, length);")
    private static native JSObject createUint8View(JSObject sab, int byteOffset, int length);

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    private static native double getFloat64(JSObject arr, int index);

    @JSBody(params = {"arr", "index"}, script = "return arr[index] | 0;")
    private static native int getInt32(JSObject arr, int index);

    @JSBody(params = {"arr", "index"}, script = "return arr[index] | 0;")
    private static native int getUint8(JSObject arr, int index);

    @JSBody(params = {"typedArray", "index"}, script = "return Atomics.load(typedArray, index);")
    private static native int atomicsLoad(JSObject typedArray, int index);
}
