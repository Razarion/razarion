package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.TestItemCockpitFrontend;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nConstants;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.MeshRenderTest;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.Collections;
import java.util.logging.Logger;

import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;
import static com.btxtech.shared.dto.FallbackConfig.FACTORY_ITEM_TYPE_ID;

public class InputServiceTest extends WeldUiBaseIntegrationTest {
    private final Logger logger = Logger.getLogger(MeshRenderTest.class.getName());

    @Test
    public void InputService() {
        // Setup
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .setUserId(1)
                .setUnlockedItemLimit(Collections.emptyMap())
                .setLevelId(1));
        setupUiEnvironment(coldGameUiContext);
        // Alarm service
        AlarmService alarmService = getWeldBean(AlarmService.class);
        alarmService.addListener(alarm -> logger.severe(alarm.toString()));
        alarmService.getAlarms().forEach(alarm -> logger.severe(alarm.toString()));
        // ???
        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();
        // I18nConstants
        I18nConstants i18nConstants = EasyMock.createNiceMock(I18nConstants.class);
        EasyMock.replay(i18nConstants);
        I18nHelper.setConstants(i18nConstants);

        setupCockpit();
        // Simulate game engine
        BaseItemUiService baseItemUiService = getWeldBean(BaseItemUiService.class);
        simulateGameEngine(baseItemUiService, coldGameUiContext);
        // Run test
        InputService inputService = getWeldBean(InputService.class);
        inputService.onMouseDown(0, 0);
        inputService.onMouseUp(20, 20);

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

    private void simulateGameEngine(BaseItemUiService baseItemUiService, ColdGameUiContext coldGameUiContext) {
        NativeSyncBaseItemTickInfo info1 = new NativeSyncBaseItemTickInfo();
        info1.id = 1;
        info1.baseId = 21;
        info1.itemTypeId = BUILDER_ITEM_TYPE_ID;
        info1.x = 10;
        info1.y = 10;
        info1.z = 0;
        info1.spawning = 1;
        info1.health = 1;
        info1.buildup = 1;

        NativeSyncBaseItemTickInfo info2 = new NativeSyncBaseItemTickInfo();
        info2.id = 2;
        info2.baseId = 21;
        info2.itemTypeId = FACTORY_ITEM_TYPE_ID;
        info2.x = 12;
        info2.y = 10;
        info2.z = 0;
        info2.spawning = 1;
        info2.health = 1;
        info2.buildup = 1;


        baseItemUiService.addBase(new PlayerBaseDto()
                .name("Test Base")
                .baseId(info1.baseId)
                .userId(coldGameUiContext.getUserContext().getUserId())
                .character(Character.HUMAN));
        baseItemUiService.updateSyncBaseItems(new NativeSyncBaseItemTickInfo[]{info1, info2});
    }


}


