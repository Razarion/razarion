package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.Material;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public class MaterialAssetType extends AssetType {
    private final Material material;

    public MaterialAssetType(Meta meta) {
        super(meta);
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.findAndRegisterModules();
            material = mapper.readValue(getMeta().getAssetFile(), MaterialHolder.class).getMaterial();
        } catch (IOException e) {
            throw new RuntimeException("Error reading: " + getMeta().getAssetFile(), e);
        }
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return "MaterialAssetType{" +
                "meta=" + getMeta() +
                "material=" + material +
                '}';
    }

    public static class MaterialHolder {
        @JsonProperty("Material")
        private Material material;

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }
    }
}
