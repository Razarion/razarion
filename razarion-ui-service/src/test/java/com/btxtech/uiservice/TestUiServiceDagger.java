package com.btxtech.uiservice;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerChecker;
import com.btxtech.uiservice.mock.BabylonRenderServiceAccessMock;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = TestUiServiceDaggerModule.class)
public interface TestUiServiceDagger {
    InitializeService initializeService();

    ItemTypeService itemTypeService();

    GameUiControl gameUiControl();

    MainCockpitService mainCockpitService();

    BaseItemPlacerChecker baseItemPlacerChecker();

    InputService inputService();

    TerrainUiService terrainUiService();

    BabylonRenderServiceAccessMock babylonRenderServiceAccessMock();

    ResourceUiService resourceUiService();

    BoxUiService boxUiService();
}
