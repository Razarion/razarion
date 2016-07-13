package com.btxtech.webglemulator.razarion;

import com.btxtech.servercommon.collada.ColladaConverterTextureMapper;

/**
 * Created by Beat
 * 13.07.2016.
 */
public class DummyColladaConverterTextureMapper implements ColladaConverterTextureMapper {
    @Override
    public Integer getTextureId(String materialId) {
        return -1;
    }
}
