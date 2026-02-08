package com.btxtech.worker.jso;

import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SharedTickBufferLayout;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * Worker-side writer for the SharedArrayBuffer double-buffer tick data transfer.
 * Writes NativeTickInfo directly into shared memory, then swaps the active buffer
 * using Atomics for lock-free synchronization with the main thread reader.
 */
public class SharedTickBufferWriter {
    private final JSObject sharedArrayBuffer;
    private final JSObject controlInt32;
    private int writeBufferIndex = 0;
    private int writeSequence = 0;

    public SharedTickBufferWriter(JSObject sharedArrayBuffer) {
        this.sharedArrayBuffer = sharedArrayBuffer;
        this.controlInt32 = createInt32View(sharedArrayBuffer, 0, 4);
    }

    public void writeTick(NativeTickInfo tickInfo) {
        int bufferByteOffset = SharedTickBufferLayout.bufferOffset(writeBufferIndex);

        // Create typed array views for the current write buffer
        JSObject headerView = createInt32View(sharedArrayBuffer, bufferByteOffset, SharedTickBufferLayout.HEADER_BYTES / 4);

        NativeSyncBaseItemTickInfo[] items = tickInfo.updatedNativeSyncBaseItemTickInfos;
        int itemCount = items != null ? items.length : 0;
        if (itemCount > SharedTickBufferLayout.MAX_ITEMS) {
            atomicsStore(controlInt32, SharedTickBufferLayout.CTRL_OVERFLOW, 1);
            itemCount = SharedTickBufferLayout.MAX_ITEMS;
        }

        NativeSimpleSyncBaseItemTickInfo[] killed = tickInfo.killedSyncBaseItems;
        int killedCount = killed != null ? killed.length : 0;
        if (killedCount > SharedTickBufferLayout.MAX_KILLED) {
            killedCount = SharedTickBufferLayout.MAX_KILLED;
        }

        int[] removeIds = tickInfo.removeSyncBaseItemIds;
        int removeCount = removeIds != null ? removeIds.length : 0;
        if (removeCount > SharedTickBufferLayout.MAX_REMOVE) {
            removeCount = SharedTickBufferLayout.MAX_REMOVE;
        }

        // Write header
        setInt32(headerView, 0, itemCount);
        setInt32(headerView, 1, killedCount);
        setInt32(headerView, 2, removeCount);
        setInt32(headerView, 3, tickInfo.resources);
        setInt32(headerView, 4, tickInfo.xpFromKills);
        setInt32(headerView, 5, tickInfo.houseSpace);

        // Write items
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

            int containingOffset = 0;
            for (int i = 0; i < itemCount; i++) {
                NativeSyncBaseItemTickInfo item = items[i];
                int dOff = i * SharedTickBufferLayout.DOUBLES_PER_ITEM;
                int iOff = i * SharedTickBufferLayout.INTS_PER_ITEM;

                // Float64: 14 doubles per item
                setFloat64(itemDoubles, dOff + 0, item.x);
                setFloat64(itemDoubles, dOff + 1, item.y);
                setFloat64(itemDoubles, dOff + 2, item.z);
                setFloat64(itemDoubles, dOff + 3, item.angle);
                setFloat64(itemDoubles, dOff + 4, item.turretAngle);
                setFloat64(itemDoubles, dOff + 5, item.spawning);
                setFloat64(itemDoubles, dOff + 6, item.buildup);
                setFloat64(itemDoubles, dOff + 7, item.health);
                setFloat64(itemDoubles, dOff + 8, item.constructing);
                setFloat64(itemDoubles, dOff + 9, item.maxContainingRadius);
                if (item.harvestingResourcePosition != null) {
                    setFloat64(itemDoubles, dOff + 10, item.harvestingResourcePosition.x);
                    setFloat64(itemDoubles, dOff + 11, item.harvestingResourcePosition.y);
                } else {
                    setFloat64(itemDoubles, dOff + 10, Double.NaN);
                    setFloat64(itemDoubles, dOff + 11, Double.NaN);
                }
                if (item.buildingPosition != null) {
                    setFloat64(itemDoubles, dOff + 12, item.buildingPosition.x);
                    setFloat64(itemDoubles, dOff + 13, item.buildingPosition.y);
                } else {
                    setFloat64(itemDoubles, dOff + 12, Double.NaN);
                    setFloat64(itemDoubles, dOff + 13, Double.NaN);
                }

                // Int32: 4 ints per item
                setInt32(itemInts, iOff + 0, item.id);
                setInt32(itemInts, iOff + 1, item.itemTypeId);
                setInt32(itemInts, iOff + 2, item.baseId);
                setInt32(itemInts, iOff + 3, item.constructingBaseItemTypeId);

                // Flags
                int flags = 0;
                if (item.contained) flags |= 1;
                if (item.idle) flags |= 2;
                if (item.containingItemTypeIds != null && item.containingItemTypeIds.length > 0) flags |= 4;
                setUint8(itemFlags, i, flags);

                // Containing IDs (prefix-length encoding)
                if (item.containingItemTypeIds != null && item.containingItemTypeIds.length > 0) {
                    if (containingOffset + 1 + item.containingItemTypeIds.length <= SharedTickBufferLayout.MAX_CONTAINING_INTS) {
                        setInt32(containingView, containingOffset++, item.containingItemTypeIds.length);
                        for (int cid : item.containingItemTypeIds) {
                            setInt32(containingView, containingOffset++, cid);
                        }
                    }
                }
            }
        }

        // Write killed items
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

            for (int i = 0; i < killedCount; i++) {
                NativeSimpleSyncBaseItemTickInfo k = killed[i];
                setFloat64(killedDoubles, i * 2, k.x);
                setFloat64(killedDoubles, i * 2 + 1, k.y);
                setInt32(killedInts, i * 2, k.id);
                setInt32(killedInts, i * 2 + 1, k.itemTypeId);
                setUint8(killedFlags, i, k.contained ? 1 : 0);
            }
        }

        // Write remove IDs
        if (removeCount > 0) {
            JSObject removeView = createInt32View(sharedArrayBuffer,
                    bufferByteOffset + SharedTickBufferLayout.REMOVE_OFFSET,
                    removeCount);
            for (int i = 0; i < removeCount; i++) {
                setInt32(removeView, i, removeIds[i]);
            }
        }

        // Publish: set activeBuffer to writeBufferIndex, then increment writeSequence
        atomicsStore(controlInt32, SharedTickBufferLayout.CTRL_ACTIVE_BUFFER, writeBufferIndex);
        writeSequence++;
        atomicsStore(controlInt32, SharedTickBufferLayout.CTRL_WRITE_SEQUENCE, writeSequence);

        // Swap write buffer for next tick
        writeBufferIndex = 1 - writeBufferIndex;
    }

    // --- JS interop ---

    @JSBody(params = {"sab", "byteOffset", "length"}, script = "return new Int32Array(sab, byteOffset, length);")
    private static native JSObject createInt32View(JSObject sab, int byteOffset, int length);

    @JSBody(params = {"sab", "byteOffset", "length"}, script = "return new Float64Array(sab, byteOffset, length);")
    private static native JSObject createFloat64View(JSObject sab, int byteOffset, int length);

    @JSBody(params = {"sab", "byteOffset", "length"}, script = "return new Uint8Array(sab, byteOffset, length);")
    private static native JSObject createUint8View(JSObject sab, int byteOffset, int length);

    @JSBody(params = {"arr", "index", "value"}, script = "arr[index] = value;")
    private static native void setFloat64(JSObject arr, int index, double value);

    @JSBody(params = {"arr", "index", "value"}, script = "arr[index] = value;")
    private static native void setInt32(JSObject arr, int index, int value);

    @JSBody(params = {"arr", "index", "value"}, script = "arr[index] = value;")
    private static native void setUint8(JSObject arr, int index, int value);

    @JSBody(params = {"typedArray", "index", "value"}, script = "Atomics.store(typedArray, index, value);")
    private static native void atomicsStore(JSObject typedArray, int index, int value);
}
