package com.btxtech.shared.datatypes;

/**
 * Interface for Uint16Array that is compatible with both GWT and TeaVM.
 * Provides indexed access to height map data.
 */
public interface Uint16ArrayEmu {
    int getAt(int index);

    /**
     * Get the length of this array.
     */
    int getLength();

    /**
     * Convert the entire array to a plain Java int array.
     * This is needed to avoid WASM-GC illegal cast errors when the array
     * must be accessed from pure Java code.
     */
    int[] toJavaArray();
}
