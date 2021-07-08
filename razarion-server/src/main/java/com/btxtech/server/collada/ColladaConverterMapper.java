package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;

/**
 * Created by Beat
 * 12.07.2016.
 */
public interface ColladaConverterMapper {
    VertexContainerMaterial toVertexContainerMaterial(String materialId);

    AnimationTrigger getAnimationTrigger(String animationId);
}
