package com.btxtech.server.collada;

import com.btxtech.shared.dto.VertexContainer;

/**
 * Created by Beat
 * 11.05.2016.
 */
public interface ColladaConverterControl {
    int getObjectId();

    String getColladaString();

    VertexContainer.Type nameToType(String name);
}
