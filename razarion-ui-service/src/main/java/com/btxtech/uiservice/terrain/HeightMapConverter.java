package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;

/**
 * Converter interface for converting Uint16ArrayEmu to plain Java arrays.
 * Platform-specific implementations handle JSObject conversion issues.
 */
public interface HeightMapConverter {
    /**
     * Convert a Uint16ArrayEmu to a plain Java int array.
     * This must be safe to call from any context.
     */
    int[] convert(Uint16ArrayEmu heightMap);
}
