package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.TestItemCockpitFrontend;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.ViewField;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.FACTORY_ITEM_TYPE_ID;

public class InputServiceTest extends WeldUiBaseIntegrationTest {

    @Test
    public void InputService() {
        // Setup
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .setUserId(1)
                .setUnlockedItemLimit(Collections.emptyMap())
                .setLevelId(1));
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

        // TODO Run selection test
//  TODO      InputService inputService = getWeldBean(InputService.class);
//        inputService.onMouseDown(0, 0);
//        inputService.onMouseUp(20, 20);
//        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        // Run ThreeJsRendererServiceAccess test
        getThreeJsRendererServiceAccessMock().clear();

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(0, 0))
                .bottomRight(new DecimalPosition(20, 0))
                .topRight(new DecimalPosition(20, 20))
                .topLeft(new DecimalPosition(0, 20)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(30, 30))
                .bottomRight(new DecimalPosition(50, 30))
                .topRight(new DecimalPosition(50, 50))
                .topLeft(new DecimalPosition(30, 50)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().size());
        Assert.assertTrue(getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().get(0).isRemoved());
        Assert.assertTrue(getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().get(1).isRemoved());
        getThreeJsRendererServiceAccessMock().clear();

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(0, 0))
                .bottomRight(new DecimalPosition(20, 0))
                .topRight(new DecimalPosition(20, 20))
                .topLeft(new DecimalPosition(0, 20)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().size());

        callOnViewChanged(new ViewField(0)
                .bottomLeft(new DecimalPosition(1, 1))
                .bottomRight(new DecimalPosition(21, 1))
                .topRight(new DecimalPosition(21, 21))
                .topLeft(new DecimalPosition(1, 21)));
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        Assert.assertEquals(2, getThreeJsRendererServiceAccessMock().getBabylonBaseItemMocks().size());
    }

    private void setupCockpit() {
        getWeldBean(ItemCockpitService.class).init(getWeldBean(TestItemCockpitFrontend.class));
        getWeldBean(MainCockpitService.class).init(new MainCockpit() {
            @Override
            public void show() {

            }

            @Override
            public void hide() {

            }

            @Override
            public void displayResources(int resources) {

            }

            @Override
            public void displayXps(int xp, int xp2LevelUp) {

            }

            @Override
            public void displayLevel(int levelNumber) {

            }

            @Override
            public Rectangle getInventoryDialogButtonLocation() {
                return null;
            }

            @Override
            public Rectangle getScrollHomeButtonLocation() {
                return null;
            }

            @Override
            public void displayItemCount(int itemCount, int houseSpace) {

            }

            @Override
            public void displayEnergy(int consuming, int generating) {

            }

            @Override
            public void showRadar(GameUiControl.RadarState radarState) {

            }

            @Override
            public void clean() {

            }
        });
    }

    private NativeSyncBaseItemTickInfo[] setup2BaseItems(int baseId) {
        NativeSyncBaseItemTickInfo info1 = new NativeSyncBaseItemTickInfo();
        info1.id = 1;
        info1.baseId = baseId;
        info1.itemTypeId = BUILDER_ITEM_TYPE_ID;
        info1.x = 10;
        info1.y = 10;
        info1.z = 0;
        info1.spawning = 1;
        info1.health = 1;
        info1.buildup = 1;

        NativeSyncBaseItemTickInfo info2 = new NativeSyncBaseItemTickInfo();
        info2.id = 2;
        info2.baseId = baseId;
        info2.itemTypeId = FACTORY_ITEM_TYPE_ID;
        info2.x = 12;
        info2.y = 10;
        info2.z = 0;
        info2.spawning = 1;
        info2.health = 1;
        info2.buildup = 1;

        return new NativeSyncBaseItemTickInfo[]{info1, info2};
    }

    private void createBase(int userId, int baseId) {
        getWeldBean(BaseItemUiService.class).addBase(new PlayerBaseDto()
                .name("Test Base")
                .baseId(baseId)
                .userId(userId)
                .character(Character.HUMAN));
    }

}


