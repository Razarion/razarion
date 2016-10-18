package com.btxtech.shared.datatypes.shape;

import java.util.Map;

/**
 * Created by Beat
 * 21.08.2016.
 */
public class Shape3DConfig {
    private int dbId;
    private String colladaString;
    private Map<String, Integer> textures;
    private Map<String, Integer> lookUpTextures;
    private Map<String, AnimationTrigger> animations;

    public int getDbId() {
        return dbId;
    }

    public Shape3DConfig setDbId(int dbId) {
        this.dbId = dbId;
        return this;
    }

    public String getColladaString() {
        return colladaString;
    }

    public Shape3DConfig setColladaString(String colladaString) {
        this.colladaString = colladaString;
        return this;
    }

    public Map<String, Integer> getTextures() {
        return textures;
    }

    public Shape3DConfig setTextures(Map<String, Integer> textures) {
        this.textures = textures;
        return this;
    }

    public Map<String, Integer> getLookUpTextures() {
        return lookUpTextures;
    }

    public void setLookUpTextures(Map<String, Integer> lookUpTextures) {
        this.lookUpTextures = lookUpTextures;
    }

    public Map<String, AnimationTrigger> getAnimations() {
        return animations;
    }

    public Shape3DConfig setAnimations(Map<String, AnimationTrigger> animations) {
        this.animations = animations;
        return this;
    }

    @Override
    public String toString() {
        return "Shape3DConfig{" +
                "dbId=" + dbId +
                ", colladaString='" + colladaString + '\'' +
                ", textures=" + textures +
                ", animations=" + animations +
                '}';
    }
}
