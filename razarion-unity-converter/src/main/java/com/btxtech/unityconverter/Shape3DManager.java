package com.btxtech.unityconverter;

import com.btxtech.unityconverter.unity.asset.type.Fbx;

import java.util.HashMap;
import java.util.Map;

public class Shape3DManager {
    private final Map<String, Integer> shape3Ds = new HashMap<>();

    public Shape3DManager() {
        this.shape3Ds.put("e449f791897e9da408437dfc51ec9045", 39); // Vehicles Constructor\Parts\MainParts\Vehicles_main_part.fbx
        this.shape3Ds.put("0c9e91a6891986140a2f6c7a3979527d", 40); // Vehicles Constructor\Parts\Bumpers\Bumpers1.fbx
    }

    public Integer getShape3DId4Fbx(Fbx fbx) {
        Integer shape3DId = shape3Ds.get(fbx.getGuid());
        if (shape3DId == null) {
            throw new RuntimeException("No Shape3D for '" + fbx.getGuid() + "' +  File: " + fbx);
        }
        return shape3DId;
    }
}
