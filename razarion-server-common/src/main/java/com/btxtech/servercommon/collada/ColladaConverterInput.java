package com.btxtech.servercommon.collada;

/**
 * Created by Beat
 * 12.07.2016.
 */
@Deprecated
public class ColladaConverterInput {
    private String colladaString;
    private int id;
    private ColladaConverterMapper textureMapper;

    public ColladaConverterInput setColladaString(String colladaString) {
        this.colladaString = colladaString;
        return this;
    }

    public ColladaConverterInput setId(int id) {
        this.id = id;
        return this;
    }

    public ColladaConverterInput setTextureMapper(ColladaConverterMapper textureMapper) {
        this.textureMapper = textureMapper;
        return this;
    }

    public int getId() {
        return id;
    }

    public String getColladaString() {
        return colladaString;
    }

    public Integer getTextureId(String materialId) {
        if(textureMapper == null) {
            return null;
        }
        return textureMapper.getTextureId(materialId);
    }
}
