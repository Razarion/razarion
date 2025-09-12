package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.btxtech.shared.dto.FallbackConfig.FACTORY_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.SHIP_ATTACKER_ITEM_TYPE_ID;
import static com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto.from;

public class InputServiceTest extends DaggerUiBaseIntegrationTest {

    @Test
    public void inputService() {
        // Setup
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId("00001")
                .unlockedItemLimit(Collections.emptyMap())
                .levelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();
        GameUiControl gameUiControl = getTestUiServiceDagger().gameUiControl();
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        setupCockpit();

        // Setup game engine
        int baseId = 1;
        createBase(coldGameUiContext.getUserContext().getUserId(), baseId);
        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = setup2BaseItems(baseId);

        BaseItemUiService baseItemUiService = getTestUiServiceDagger().baseItemUiService();

        // Run ThreeJsRendererServiceAccess test
        getBabylonRendererServiceAccessMock().clear();
        callOnViewChanged(
                0, 0,
                20, 0,
                20, 20,
                0, 20);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        getTestUiServiceDagger().selectionService().onBaseItemsSelected(List.of(from(nativeSyncBaseItemTickInfos[0])));
        getTestUiServiceDagger().inputService().terrainClicked(new DecimalPosition(0, 0));

        showDisplay();

        callOnViewChanged(
                0, 0,
                20, 0,
                20, 20,
                0, 20);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());
        Assert.assertTrue(getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().get(0).isDisposed());
        Assert.assertTrue(getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().get(1).isDisposed());
        getBabylonRendererServiceAccessMock().clear();

        callOnViewChanged(
                0, 0,
                20, 0,
                20, 20,
                0, 20);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        callOnViewChanged(
                0, 0,
                20, 0,
                20, 20,
                0, 20);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());
    }

    private NativeSyncBaseItemTickInfo[] setup2BaseItems(int baseId) {
        NativeSyncBaseItemTickInfo info1 = new NativeSyncBaseItemTickInfo();
        info1.id = 1;
        info1.baseId = baseId;
        info1.itemTypeId = SHIP_ATTACKER_ITEM_TYPE_ID;
        info1.x = 10;
        info1.y = 10;
        info1.spawning = 1;
        info1.health = 1;
        info1.buildup = 1;

        NativeSyncBaseItemTickInfo info2 = new NativeSyncBaseItemTickInfo();
        info2.id = 2;
        info2.baseId = baseId;
        info2.itemTypeId = FACTORY_ITEM_TYPE_ID;
        info2.x = 12;
        info2.y = 10;
        info2.spawning = 1;
        info2.health = 1;
        info2.buildup = 1;

        return new NativeSyncBaseItemTickInfo[]{info1, info2};
    }


}


