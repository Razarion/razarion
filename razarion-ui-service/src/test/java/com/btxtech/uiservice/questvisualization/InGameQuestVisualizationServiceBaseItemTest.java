package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.mock.BabylonRenderServiceAccessMock;
import com.btxtech.uiservice.terrain.InputService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class InGameQuestVisualizationServiceBaseItemTest extends DaggerUiBaseIntegrationTest {
    private static final int OWN_BASE_ITEM_ID = 1;
    private static final int OTHER_BASE_ITEM_ID = 2;
    private static final int BOT_BASE_ITEM_ID = 3;

    @Test
    public void activateBaseItemInsideScrollOutside() {
        ColdGameUiContext coldGameUiContext = setup();

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[2];
        NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 1;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.BUILDER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 140;
        nativeSyncBaseItemTickInfo.y = 175;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[0] = nativeSyncBaseItemTickInfo;

        nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 2;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.BUILDER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 160;
        nativeSyncBaseItemTickInfo.y = 175;
        nativeSyncBaseItemTickInfo.baseId = OTHER_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[1] = nativeSyncBaseItemTickInfo;

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        createBase(coldGameUiContext.getUserContext().getUserId(), OWN_BASE_ITEM_ID);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).comparisonConfig(new ComparisonConfig().count(10))));

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        BabylonRenderServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRenderServiceAccessMock.class);
        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(2, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        Assert.assertNotNull(findBaseItem(1).getMarkerConfig());
        Assert.assertNull(findBaseItem(2).getMarkerConfig());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(264.6, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
    }

    @Test
    public void activateBaseItemOutsideScrollInside() {
        ColdGameUiContext coldGameUiContext = setup();

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[2];
        NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 1;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.BUILDER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 160;
        nativeSyncBaseItemTickInfo.y = 250;
        nativeSyncBaseItemTickInfo.baseId = BOT_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[0] = nativeSyncBaseItemTickInfo;

        nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = 2;
        nativeSyncBaseItemTickInfo.itemTypeId = FallbackConfig.BUILDER_ITEM_TYPE_ID;
        nativeSyncBaseItemTickInfo.x = 140;
        nativeSyncBaseItemTickInfo.y = 250;
        nativeSyncBaseItemTickInfo.baseId = OTHER_BASE_ITEM_ID;
        nativeSyncBaseItemTickInfos[1] = nativeSyncBaseItemTickInfo;

        createBase(coldGameUiContext.getUserContext().getUserId(), OWN_BASE_ITEM_ID);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).comparisonConfig(new ComparisonConfig().count(10))));

        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        BabylonRenderServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRenderServiceAccessMock.class);
        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(85.6, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);
        getWeldBean(BaseItemUiService.class).updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(2, babylonRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        Assert.assertNotNull(findBaseItem(1).getMarkerConfig());
        Assert.assertNull(findBaseItem(2).getMarkerConfig());
    }

    private ColdGameUiContext setup() {
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
                60, 40,
                240, 40,
                300, 200,
                0, 200);

        setupCockpit();

        getWeldBean(BaseItemUiService.class).addBase(new PlayerBaseDto()
                .name("Other human test base")
                .baseId(OTHER_BASE_ITEM_ID)
                .userId("00010")
                .character(Character.HUMAN));
        getWeldBean(BaseItemUiService.class).addBase(new PlayerBaseDto()
                .name("Bot test base")
                .baseId(BOT_BASE_ITEM_ID)
                .userId("00011")
                .character(Character.BOT));

        return coldGameUiContext;
    }
}