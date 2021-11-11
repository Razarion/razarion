package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.asset.Mesh;
import com.btxtech.shared.datatypes.asset.MeshContainer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class AssetService {
    private Map<Integer, MeshContainer> meshContainers;

    @PostConstruct
    public void init() {
        meshContainers = new HashMap<>();
        meshContainers.put(1, new MeshContainer().children(Arrays.asList(
                        new MeshContainer().mesh(new Mesh().shape3DId(39).element3DId("Base02")),
                        new MeshContainer().mesh(new Mesh().shape3DId(39).element3DId("ArmoredCabin02"))
                )
        )); // TODO Remove
    }

    public MeshContainer getMeshContainer(int meshContainerId) {
        return meshContainers.get(meshContainerId);
    }
}
