package com.btxtech.shared.gameengine.datatypes.workerdto;

/**
 * Layout constants for the SharedArrayBuffer double-buffer used to transfer tick data
 * from the Worker thread to the Main thread without postMessage overhead.
 *
 * Memory Layout (all offsets in bytes):
 *   [0..15]   Control Region (4 x Int32)
 *   [16..]    Buffer A
 *   [16 + BUFFER_SIZE..]  Buffer B
 *
 * Per-buffer layout:
 *   Header:   3 ints (12 bytes) = itemCount, killedCount, removeCount
 *   Scalars:  3 ints (12 bytes) = resources, xpFromKills, houseSpace
 *   Items:    MAX_ITEMS * BYTES_PER_ITEM
 *   Containing: MAX_CONTAINING_INTS * 4
 *   Killed:   MAX_KILLED * BYTES_PER_KILLED
 *   Remove:   MAX_REMOVE * 4
 */
public final class SharedTickBufferLayout {

    private SharedTickBufferLayout() {
    }

    // --- Capacity limits ---
    public static final int MAX_ITEMS = 512;
    public static final int MAX_KILLED = 64;
    public static final int MAX_CONTAINING_INTS = 2048;
    public static final int MAX_REMOVE = 64;

    // --- Per-item sizes (matching TypedArray encoding) ---
    public static final int DOUBLES_PER_ITEM = 14;
    public static final int INTS_PER_ITEM = 4;
    public static final int FLAGS_PER_ITEM = 1;
    // Bytes per item: 14*8 + 4*4 + 1 = 112 + 16 + 1 = 129
    public static final int BYTES_PER_ITEM = DOUBLES_PER_ITEM * 8 + INTS_PER_ITEM * 4 + FLAGS_PER_ITEM;

    // --- Per-killed sizes ---
    public static final int KILLED_DOUBLES_PER_ITEM = 2;
    public static final int KILLED_INTS_PER_ITEM = 2;
    public static final int KILLED_FLAGS_PER_ITEM = 1;
    // Bytes per killed: 2*8 + 2*4 + 1 = 16 + 8 + 1 = 25
    public static final int BYTES_PER_KILLED = KILLED_DOUBLES_PER_ITEM * 8 + KILLED_INTS_PER_ITEM * 4 + KILLED_FLAGS_PER_ITEM;

    // --- Control region (Int32 indices) ---
    public static final int CTRL_ACTIVE_BUFFER = 0;
    public static final int CTRL_WRITE_SEQUENCE = 1;
    public static final int CTRL_READ_SEQUENCE = 2;
    public static final int CTRL_OVERFLOW = 3;
    public static final int CONTROL_BYTES = 16; // 4 x Int32

    // --- Per-buffer header (byte offsets relative to buffer start) ---
    public static final int HDR_ITEM_COUNT = 0;       // Int32
    public static final int HDR_KILLED_COUNT = 4;      // Int32
    public static final int HDR_REMOVE_COUNT = 8;      // Int32
    public static final int HDR_RESOURCES = 12;         // Int32
    public static final int HDR_XP_FROM_KILLS = 16;     // Int32
    public static final int HDR_HOUSE_SPACE = 20;       // Int32
    public static final int HEADER_BYTES = 24;          // 6 x Int32

    // --- Section offsets within a buffer (relative to buffer start) ---
    // Items section: starts right after header
    // Each item: 14 doubles (Float64), 4 ints (Int32), 1 byte flags (Uint8)
    // We lay them out as: all doubles, then all ints, then all flags
    public static final int ITEMS_DOUBLES_OFFSET = HEADER_BYTES;
    public static final int ITEMS_DOUBLES_BYTES = MAX_ITEMS * DOUBLES_PER_ITEM * 8;

    public static final int ITEMS_INTS_OFFSET = ITEMS_DOUBLES_OFFSET + ITEMS_DOUBLES_BYTES;
    public static final int ITEMS_INTS_BYTES = MAX_ITEMS * INTS_PER_ITEM * 4;

    public static final int ITEMS_FLAGS_OFFSET = ITEMS_INTS_OFFSET + ITEMS_INTS_BYTES;
    public static final int ITEMS_FLAGS_BYTES = MAX_ITEMS * FLAGS_PER_ITEM;

    // Containing IDs section (Int32 prefix-length encoded)
    public static final int CONTAINING_OFFSET = ITEMS_FLAGS_OFFSET + ITEMS_FLAGS_BYTES;
    public static final int CONTAINING_BYTES = MAX_CONTAINING_INTS * 4;

    // Killed section: doubles, ints, flags
    public static final int KILLED_DOUBLES_OFFSET = CONTAINING_OFFSET + CONTAINING_BYTES;
    public static final int KILLED_DOUBLES_BYTES = MAX_KILLED * KILLED_DOUBLES_PER_ITEM * 8;

    public static final int KILLED_INTS_OFFSET = KILLED_DOUBLES_OFFSET + KILLED_DOUBLES_BYTES;
    public static final int KILLED_INTS_BYTES = MAX_KILLED * KILLED_INTS_PER_ITEM * 4;

    public static final int KILLED_FLAGS_OFFSET = KILLED_INTS_OFFSET + KILLED_INTS_BYTES;
    public static final int KILLED_FLAGS_BYTES = MAX_KILLED * KILLED_FLAGS_PER_ITEM;

    // Remove IDs section (Int32)
    public static final int REMOVE_OFFSET = KILLED_FLAGS_OFFSET + KILLED_FLAGS_BYTES;
    public static final int REMOVE_BYTES = MAX_REMOVE * 4;

    // Total buffer size (one buffer)
    public static final int BUFFER_SIZE = REMOVE_OFFSET + REMOVE_BYTES;

    // Buffer start offsets (byte offsets from SharedArrayBuffer start)
    public static final int BUFFER_A_OFFSET = CONTROL_BYTES;
    public static final int BUFFER_B_OFFSET = CONTROL_BYTES + BUFFER_SIZE;

    // Total SharedArrayBuffer size
    public static final int TOTAL_BYTES = CONTROL_BYTES + 2 * BUFFER_SIZE;

    /**
     * Get the byte offset for a given buffer index (0 or 1).
     */
    public static int bufferOffset(int bufferIndex) {
        return bufferIndex == 0 ? BUFFER_A_OFFSET : BUFFER_B_OFFSET;
    }
}
