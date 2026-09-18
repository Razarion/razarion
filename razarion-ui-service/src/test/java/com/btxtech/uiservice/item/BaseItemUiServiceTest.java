package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.mock.BabylonRenderServiceAccessMock;
import com.btxtech.uiservice.terrain.InputService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.btxtech.shared.dto.FallbackConfig.ATTACKER_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.FACTORY_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.GENERATOR_ITEM_TYPE_ID;

public class BaseItemUiServiceTest extends DaggerUiBaseIntegrationTest {
    @Test
    public void test() {
        // Init
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId("00001")
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

        setupCockpit();

        // Runtime
        createBase(coldGameUiContext.getUserContext().getUserId(), 21);

        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
        info.id = 1;
        info.baseId = 21;
        info.itemTypeId = BUILDER_ITEM_TYPE_ID;
        info.x = 274;
        info.y = 100;
        info.spawning = 1;
        info.health = 1;
        info.buildup = 1;

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[1];
        nativeSyncBaseItemTickInfos[0] = info;

        BaseItemUiService baseItemUiService = getWeldBean(BaseItemUiService.class);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        BabylonRenderServiceAccessMock threeJsRendererServiceAccessMock = getWeldBean(BabylonRenderServiceAccessMock.class);
        Assert.assertEquals(1, threeJsRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        BabylonRenderServiceAccessMock.BabylonBaseItemMock babylonBaseItemMock = threeJsRendererServiceAccessMock.getBabylonBaseItemMocks().get(0);
        // TODO assertDecimalPosition(new DecimalPosition(274, 100), babylonBaseItemMock.getPosition());
        Assert.assertEquals(0, babylonBaseItemMock.getAngle(), 0.0001);
        Assert.assertEquals(babylonBaseItemMock.getDiplomacy(), Diplomacy.OWN);
        Assert.assertFalse(babylonBaseItemMock.isSelect());
        Assert.assertFalse(babylonBaseItemMock.isHover());
    }

    /**
     * The tips read every own item and the quest's enemies from here, off screen included - no
     * view field is set, so none of the items is rendered. Through Dagger directly: getWeldBean() is a
     * leftover of the Weld harness and returns null, which is why test() above fails.
     */
    @Test
    public void tipItemStates() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId("00001")
                .unlockedItemLimit(Collections.emptyMap())
                .levelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();
        createBase(coldGameUiContext.getUserContext().getUserId(), 21);
        getTestUiServiceDagger().baseItemUiService().addBase(new PlayerBaseDto()
                .name("Bot")
                .baseId(22)
                .character(Character.BOT));

        NativeSyncBaseItemTickInfo builder = tickInfo(1, 21, BUILDER_ITEM_TYPE_ID, 500, 600);
        builder.idle = false;
        NativeSyncBaseItemTickInfo factory = tickInfo(2, 21, FACTORY_ITEM_TYPE_ID, 510, 600);
        factory.buildup = 0.4;
        factory.factoryBuildQueue = new int[]{ATTACKER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID};
        NativeSyncBaseItemTickInfo spawning = tickInfo(3, 21, ATTACKER_ITEM_TYPE_ID, 520, 600);
        spawning.spawning = 0.5;
        NativeSyncBaseItemTickInfo contained = tickInfo(4, 21, ATTACKER_ITEM_TYPE_ID, 530, 600);
        contained.contained = true;
        NativeSyncBaseItemTickInfo botGenerator = tickInfo(5, 22, GENERATOR_ITEM_TYPE_ID, 800, 600);
        NativeSyncBaseItemTickInfo botAttacker = tickInfo(6, 22, ATTACKER_ITEM_TYPE_ID, 810, 600);

        BaseItemUiService baseItemUiService = getTestUiServiceDagger().baseItemUiService();
        baseItemUiService.updateSyncBaseItems(new NativeSyncBaseItemTickInfo[]{builder, factory, spawning, contained, botGenerator, botAttacker});

        TipItemState[] typed = baseItemUiService.getTipItemStates(GENERATOR_ITEM_TYPE_ID);
        Assert.assertEquals(3, typed.length);
        Assert.assertEquals(1, typed[0].id);
        Assert.assertTrue(typed[0].own);
        Assert.assertFalse(typed[0].idle);
        Assert.assertEquals(500, typed[0].x, 0.0001);
        Assert.assertEquals(0, typed[0].factoryBuildQueue.length);
        Assert.assertEquals(2, typed[1].id);
        Assert.assertEquals(0.4, typed[1].buildup, 0.0001);
        Assert.assertArrayEquals(new int[]{ATTACKER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID}, typed[1].factoryBuildQueue);
        Assert.assertEquals(5, typed[2].id);
        Assert.assertFalse(typed[2].own);

        Assert.assertEquals(4, baseItemUiService.getTipItemStates(0).length);
        Assert.assertEquals(2, baseItemUiService.getTipItemStates(-1).length);
    }

    private NativeSyncBaseItemTickInfo tickInfo(int id, int baseId, int itemTypeId, double x, double y) {
        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
        info.id = id;
        info.baseId = baseId;
        info.itemTypeId = itemTypeId;
        info.x = x;
        info.y = y;
        info.spawning = 1;
        info.health = 1;
        info.buildup = 1;
        info.idle = true;
        return info;
    }
}
