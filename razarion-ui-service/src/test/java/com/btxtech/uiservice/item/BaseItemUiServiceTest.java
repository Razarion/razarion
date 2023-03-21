package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.nativejs.NativeMatrixDto;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.ThreeJsRendererServiceAccessMock;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.BabylonBaseItemState;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;

public class BaseItemUiServiceTest extends WeldUiBaseIntegrationTest {
    private final Logger logger = Logger.getLogger(BaseItemUiServiceTest.class.getName());

    @Test
    public void test() {
        // Init
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();

        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        getWeldBean(TerrainUiService.class).setLoaded();

        getWeldBean(InputService.class).onViewFieldChanged(
                250, 50,
                300, 50,
                500, 200,
                200, 200);

        // Runtime
        NativeMatrixDto nativeMatrixDto = new NativeMatrixDto();
        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
        info.id = 1;
        info.baseId = 21;
        info.itemTypeId = BUILDER_ITEM_TYPE_ID;
        info.x = 274;
        info.y = 100;
        info.z = 2;
        nativeMatrixDto.numbers = Matrix4.createTranslation(info.x, info.y, info.z).toArray();
        info.model = nativeMatrixDto;
        info.spawning = 1;
        info.health = 1;
        info.buildup = 1;

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[1];
        nativeSyncBaseItemTickInfos[0] = info;

        BaseItemUiService baseItemUiService = getWeldBean(BaseItemUiService.class);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);

        ThreeJsRendererServiceAccessMock threeJsRendererServiceAccessMock = getWeldBean(ThreeJsRendererServiceAccessMock.class);
        Assert.assertEquals(1, threeJsRendererServiceAccessMock.getBabylonBaseItemMocks().size());
        ThreeJsRendererServiceAccessMock.BabylonBaseItemMock babylonBaseItemMock = threeJsRendererServiceAccessMock.getBabylonBaseItemMocks().get(0);
        Assert.assertEquals(1, babylonBaseItemMock.getBabylonBaseItemStates().size());
        BabylonBaseItemState state = babylonBaseItemMock.getBabylonBaseItemStates().get(0);
        Assert.assertEquals(274, state.xPos, 0.0001);
        Assert.assertEquals(100, state.yPos, 0.0001);
        Assert.assertEquals(2, state.zPos, 0.0001);

    }
}
