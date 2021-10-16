package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.Meta;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class AssetTypeFactory {
    public static final String META = "meta";
    private static final HashMap<String, Class<? extends AssetType>> asstTypes = new HashMap<>();

    static {
        asstTypes.put("prefab", Prefab.class);
        asstTypes.put("fbx", Fbx.class);
        asstTypes.put("mat", Material.class);
        asstTypes.put("png", Image.class);
        asstTypes.put("cs", Ignore.class);
        asstTypes.put("unity", Ignore.class);
        asstTypes.put("shadergraph", Ignore.class);
    }

    public static AssetType create(Meta meta) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<? extends AssetType> assetTypeClass = asstTypes.get(meta.getFileExtension());
        return assetTypeClass.getDeclaredConstructor(Meta.class).newInstance(meta);
    }

}
