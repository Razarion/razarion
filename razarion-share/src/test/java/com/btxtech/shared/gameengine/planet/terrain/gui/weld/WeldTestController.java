package com.btxtech.shared.gameengine.planet.terrain.gui.weld;

import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 09.04.2017.
 */
@Singleton
public class WeldTestController extends AbstractTerrainTestController {
    @Inject
    private WeldTestRenderer weldTestRenderer;
    private Object[] userObjects;

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        weldTestRenderer.setupFields(userObjects);
        return weldTestRenderer;
    }

    public void setUserObjects(Object[] userObjects) {
        this.userObjects = userObjects;
    }
}
