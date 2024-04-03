package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.BabylonRendererServiceAccessMock;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;
import static com.btxtech.test.shared.SharedTestHelper.assertVertex;

public class BaseItemUiServiceTest extends WeldUiBaseIntegrationTest {
    @Test
    public void test() {
        // Init
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId(1)
                .unlockedItemLimit(Collections.emptyMap())
                .levelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();

        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        getWeldBean(InputService.class).onViewFieldChanged(
                250, 50,
                300, 50,
                500, 200,
                200, 200);

        setupI18nConstants();
        setupCockpit();

        // Runtime
        createBase(coldGameUiContext.getUserContext().getUserId(), 21);

        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
        info.id = 1;
        info.baseId = 21;
        info.itemTypeId = BUILDER_ITEM_TYPE_ID;
        info.x = 274;
        info.y = 100;
        info.z = 2;
        info.spawning = 1;
        info.health = 1;
        info.buildup = 1;

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[1];
        nativeSyncBaseItemTickInfos[0] = info;

        BaseItemUiService baseItemUiService = getWeldBean(BaseItemUiService.class);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        BabylonRendererServiceAccessMock threeJsRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        Assert.assertEquals(1, threeJsRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        BabylonRendererServiceAccessMock.BabylonBaseItemMock babylonBaseItemMock = threeJsRendererServiceAccessMock.getBabylonBaseItemMocks().get(0);
        assertVertex(274, 100, 2, babylonBaseItemMock.getPosition());
        Assert.assertEquals(0, babylonBaseItemMock.getAngle(), 0.0001);
        Assert.assertEquals(babylonBaseItemMock.getDiplomacy(), Diplomacy.OWN);
        Assert.assertFalse(babylonBaseItemMock.isSelect());
        Assert.assertFalse(babylonBaseItemMock.isHover());
    }
}
