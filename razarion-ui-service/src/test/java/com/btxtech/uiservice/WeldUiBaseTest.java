package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.cdimock.TestSimpleExecutorService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class WeldUiBaseTest {
    // ... work in progress ...

    private WeldContainer weldContainer;
    private TestSimpleExecutorService testSimpleExecutorService;
    private BaseItemService baseItemService;
    private PlanetConfig planetConfig;
    private StaticGameConfig staticGameConfig;
    private PlanetService planetService;

    protected void setupUiEnvironment() {
        this.staticGameConfig = staticGameConfig;
        this.planetConfig = planetConfig;
        // Init weld
        Weld weld = new Weld();
        weldContainer = weld.initialize();
    }

    public <T> T getWeldBean(Class<T> clazz) {
        return weldContainer.instance().select(clazz).get();
    }

}
