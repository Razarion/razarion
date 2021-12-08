package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.Material;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class MaterialAssetType extends AssetType {
    private static final Logger LOGGER = Logger.getLogger(MaterialAssetType.class.getName());
    private Material material;

    public MaterialAssetType(Meta meta) {
        super(meta);
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.findAndRegisterModules();

            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Constructor constructor = new Constructor();

            constructor.addTypeDescription(new TypeDescription(LinkedHashMap.class, "tag:unity3d.com,2011:21")); // Material
            constructor.addTypeDescription(new TypeDescription(LinkedHashMap.class, "tag:unity3d.com,2011:114")); // MonoBehaviour -> Ignore

            Yaml yaml = new Yaml(constructor, representer, new DumperOptions());

            constructor.setComposer(new Composer(new ParserImpl(new StreamReader(new UnicodeReader(removeUnityCrap(getAssetFile())))), new Resolver()));
            yaml.composeAll(new UnicodeReader(removeUnityCrap(getAssetFile()))).forEach(node -> {
                switch (node.getTag().getValue()) {
                    case "tag:unity3d.com,2011:21": {
                        String snippet = node.getStartMark().get_snippet();
                        String objectId = readObjectId(snippet);
                        Object component = constructor.getData();
                        LinkedHashMap<String, LinkedHashMap> map = (LinkedHashMap) component;
                        map.forEach((s, childMap) -> {
                            material = mapper.convertValue(childMap, Material.class);
                            material.setObjectId(objectId);
                        });
                        break;
                    }
                    case "tag:unity3d.com,2011:114":
                        // MonoBehaviour -> Ignore
                        break;
                    default:
                        LOGGER.severe("MaterialAssetType Tag (Unity class) : " + node.getTag().getValue());
                }
            });
        } catch (Throwable e) {
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
}
