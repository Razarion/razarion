package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpawnItemType;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Dependent
public class SpanItemTypeShape3DRenderer extends Shape3DRenderer {
    @Inject
    private Instance<SpawnItemTypeElement3DRenderer> instance;
    private SpawnItemType spawnItemType;

    public void init(SpawnItemType spawnItemType) {
        this.spawnItemType = spawnItemType;
        init(spawnItemType.getShape3D());
    }

    public SpawnItemType getSpawnItemType() {
        return spawnItemType;
    }

    @Override
    protected Element3DRenderer createElement3DRenderer(Element3D element3D) {
        return instance.get();
    }
}
