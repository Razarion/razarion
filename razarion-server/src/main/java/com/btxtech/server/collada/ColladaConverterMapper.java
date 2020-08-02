package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;

/**
 * Created by Beat
 * 12.07.2016.
 */
public interface ColladaConverterMapper {
    Integer getTextureId(String materialId);

    Integer getBumpMapId(String materialId);

    Double getBumpMapDepth(String materialId);

    Double getAlphaToCoverage(String materialId);

    boolean isCharacterRepresenting(String materialId);

    AnimationTrigger getAnimationTrigger(String animationId);
}
