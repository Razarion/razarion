package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.ViewField;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.FACTORY_ITEM_TYPE_ID;

public class InputServiceTest extends DaggerUiBaseIntegrationTest {

    @Test
    public void InputService() {
        // Setup
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId(1)
                .unlockedItemLimit(Collections.emptyMap())
                .levelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();
        setupI18nConstants();
        // ???
        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        setupCockpit();

        // Setup game engine
        int baseId = 1;
        createBase(coldGameUiContext.getUserContext().getUserId(), baseId);
        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = setup2BaseItems(baseId);

        BaseItemUiService baseItemUiService = getWeldBean(BaseItemUiService.class);

        // Run ThreeJsRendererServiceAccess test
        getBabylonRendererServiceAccessMock().clear();

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(0, 0))
                .bottomRight(new DecimalPosition(20, 0))
                .topRight(new DecimalPosition(20, 20))
                .topLeft(new DecimalPosition(0, 20)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(30, 30))
                .bottomRight(new DecimalPosition(50, 30))
                .topRight(new DecimalPosition(50, 50))
                .topLeft(new DecimalPosition(30, 50)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());
        Assert.assertTrue(getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().get(0).isDisposed());
        Assert.assertTrue(getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().get(1).isDisposed());
        getBabylonRendererServiceAccessMock().clear();

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(0, 0))
                .bottomRight(new DecimalPosition(20, 0))
                .topRight(new DecimalPosition(20, 20))
                .topLeft(new DecimalPosition(0, 20)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(1, 1))
                .bottomRight(new DecimalPosition(21, 1))
                .topRight(new DecimalPosition(21, 21))
                .topLeft(new DecimalPosition(1, 21)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getBabylonRendererServiceAccessMock().getBabylonBaseItemMocks().size());
    }

    private NativeSyncBaseItemTickInfo[] setup2BaseItems(int baseId) {
        NativeSyncBaseItemTickInfo info1 = new NativeSyncBaseItemTickInfo();
        info1.id = 1;
        info1.baseId = baseId;
        info1.itemTypeId = BUILDER_ITEM_TYPE_ID;
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


