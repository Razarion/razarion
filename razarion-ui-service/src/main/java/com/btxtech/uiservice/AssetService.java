package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.asset.Mesh;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class AssetService {
    private Map<Integer, Mesh> meshes;

    @PostConstruct
    public void init() {
        meshes = new HashMap<>();
        meshes.put(1, new Mesh().shape3DId(39).element3DId("Base00")); // TODO Remove
    }

    public Mesh getMesh(int meshId) {
        return meshes.get(meshId);
    }
}
