package com.btxtech.servercommon.collada;

import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;

/**
 * Created by Beat
 * 12.07.2016.
 */
public interface ColladaConverterMapper {
    Integer getTextureId(String materialId);

    ItemState getItemState(String animationId);
}
