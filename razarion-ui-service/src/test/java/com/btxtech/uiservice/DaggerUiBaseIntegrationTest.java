package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.gui.AbstractUiTestGuiRenderer;
import com.btxtech.uiservice.gui.UiTestGuiDisplay;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.mock.BabylonRenderServiceAccessMock;
import com.btxtech.uiservice.mock.TestItemCockpitFrontend;
import com.btxtech.uiservice.mock.TestMainCockpit;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;
import javafx.scene.paint.Color;

import java.util.logging.Logger;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class DaggerUiBaseIntegrationTest {
    private final Logger LOG = Logger.getLogger(DaggerUiBaseIntegrationTest.class.getName());
    private final TestUiServiceDagger testUiServiceDagger;

    public DaggerUiBaseIntegrationTest() {
        testUiServiceDagger = DaggerTestUiServiceDagger.builder().build();
    }

    protected void setupUiEnvironment(ColdGameUiContext coldGameUiContext) {
        testUiServiceDagger.mainCockpitService().init(new TestMainCockpit());

        testUiServiceDagger.initializeService().setColdGameUiContext(coldGameUiContext);
        testUiServiceDagger.terrainUiService().setPlanetConfig(coldGameUiContext.getWarmGameUiContext().getPlanetConfig());
        testUiServiceDagger.gameUiControl().setColdGameUiContext(coldGameUiContext);
        testUiServiceDagger.gameUiControl().init();
    }

    protected void setupUiEnvironment() {
        setupUiEnvironment(new ColdGameUiContext().warmGameUiContext(new WarmGameUiContext()));
    }

    public TestUiServiceDagger getTestUiServiceDagger() {
        return testUiServiceDagger;
    }

    //----------------------------

    @Deprecated
    protected <T> T getWeldBean(Class<T> clazz) {
        // return weldContainer.instance().select(clazz).get();
        return null;
    }

    protected void callOnViewChanged(ViewField viewField) {
        getWeldBean(BaseItemUiService.class).onViewChanged(viewField, viewField.calculateAabbRectangle());
    }

    protected TerrainUiService getTerrainUiService() {
        return getWeldBean(TerrainUiService.class);
    }

    protected BabylonRenderServiceAccessMock getBabylonRendererServiceAccessMock() {
        return getWeldBean(BabylonRenderServiceAccessMock.class);
    }

    protected void setupAlarmService() {
        AlarmService alarmService = getWeldBean(AlarmService.class);
        alarmService.addListener(alarm -> LOG.severe(alarm.toString()));
        alarmService.getAlarms().forEach(alarm -> LOG.severe(alarm.toString()));
    }

    @Deprecated
    protected void setupI18nConstants() {
    }

    protected void createBase(String userId, int baseId) {
        getWeldBean(BaseItemUiService.class).addBase(new PlayerBaseDto()
                .name("Test Base")
                .baseId(baseId)
                .userId(userId)
                .character(Character.HUMAN));
    }

    protected SyncResourceItemSimpleDto createResource(int id, int itemTypeId, DecimalPosition position) {
        throw new UnsupportedOperationException("...TODO...");
//        SyncResourceItemSimpleDto syncResourceItemSimpleDto = new SyncResourceItemSimpleDto();
//        syncResourceItemSimpleDto.setId(id);
//        syncResourceItemSimpleDto.setItemTypeId(itemTypeId);
//        syncResourceItemSimpleDto.setPosition(position);
//        return syncResourceItemSimpleDto;
    }

    protected SyncBoxItemSimpleDto createBox(int id, int itemTypeId, DecimalPosition position) {
        throw new UnsupportedOperationException("...TODO...");
//        SyncBoxItemSimpleDto syncBoxItemSimpleDto = new SyncBoxItemSimpleDto();
//        syncBoxItemSimpleDto.setId(id);
//        syncBoxItemSimpleDto.setItemTypeId(itemTypeId);
//        syncBoxItemSimpleDto.setPosition(position);
//        return syncBoxItemSimpleDto;
    }

    protected BabylonRenderServiceAccessMock.BabylonBaseItemMock findBaseItem(int id) {
        BabylonRenderServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRenderServiceAccessMock.class);
        for (BabylonRenderServiceAccessMock.BabylonBaseItemMock babylonBaseItemMock : babylonRendererServiceAccessMock.getBabylonBaseItemMocks()) {
            if (babylonBaseItemMock.getId() == id) {
                return babylonBaseItemMock;
            }
        }
        throw new IllegalArgumentException("No base item with id: " + id);
    }

    protected void setupCockpit() {
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
            public void displayItemCount(int itemCount, int usedHouseSpace, int houseSpace) {

            }

            @Override
            public void displayEnergy(int consuming, int generating) {

            }

            @Override
            public void showRadar(GameUiControl.RadarState radarState) {

            }

            @Override
            public void blinkAvailableUnlock(boolean show) {

            }

            @Override
            public void clean() {

            }
        });
    }

    public void showDisplay(Object... customRender) {
        BabylonRenderServiceAccessMock babylonRendererServiceAccessMock = getTestUiServiceDagger().babylonRenderServiceAccessMock();
        ResourceItemType resourceItemType = getTestUiServiceDagger().itemTypeService().getResourceItemType(FallbackConfig.RESOURCE_ITEM_TYPE_ID);
        double radiusResource = resourceItemType.getRadius();
        BoxItemType boxItemType = getTestUiServiceDagger().itemTypeService().getBoxItemType(FallbackConfig.BOX_ITEM_TYPE_ID);
        double radiusBox = boxItemType.getRadius();
        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
                // TerrainType
                babylonRendererServiceAccessMock.getBabylonTerrainTileMocks().forEach(babylonTerrainTileMock -> {
                    for (int x = 0; x < TerrainUtil.NODE_X_COUNT; x++) {
                        for (int y = 0; y < TerrainUtil.NODE_Y_COUNT; y++) {
                            Index nodeIndex = new Index(x, y).add(TerrainUtil.tileIndexToNodeIndex(babylonTerrainTileMock.getTerrainTile().getIndex()));
                            DecimalPosition terrainPosition = TerrainUtil.nodeIndexToTerrainPosition(nodeIndex);

                            TerrainType terrainType = getTestUiServiceDagger().terrainUiService().getTerrainType(terrainPosition);
                            switch (terrainType) {
                                case WATER:
                                    getGc().setFill(Color.BLUE);
                                    break;
                                case LAND:
                                    getGc().setFill(Color.GREEN);
                                    break;
                                case BLOCKED:
                                    getGc().setFill(Color.BLUE);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown terrain type: " + terrainType);
                            }
                            getGc().fillRect(terrainPosition.getX() + 0.25, terrainPosition.getY() + 0.25, 0.5, 0.5);
                        }
                    }
                });
                // Resource marker
                babylonRendererServiceAccessMock.getBabylonResourceItemMocks().forEach(babylonResourceItemMock -> {
                    if (babylonResourceItemMock.getMarkerConfig() != null) {
                        getGc().setFill(Color.YELLOW);
                        getGc().fillOval(babylonResourceItemMock.getPosition().getX() - 2 * radiusResource,
                                babylonResourceItemMock.getPosition().getY() - 2 * radiusResource,
                                4 * radiusResource,
                                4 * radiusResource);
                    }
                });
                // Resource
                getTestUiServiceDagger().resourceUiService().getResources().forEach((integer, syncResourceItemSimpleDto) -> {
                    getGc().setFill(Color.PINK);
                    getGc().fillOval(syncResourceItemSimpleDto.getPosition().getX() - radiusResource,
                            syncResourceItemSimpleDto.getPosition().getY() - radiusResource,
                            2 * radiusResource,
                            2 * radiusResource);

                });
                // Box marker
                babylonRendererServiceAccessMock.getBabylonBoxItemMocks().forEach(babylonBoxItemMock -> {
                    if (babylonBoxItemMock.getMarkerConfig() != null) {
                        getGc().setFill(Color.YELLOW);
                        getGc().fillOval(babylonBoxItemMock.getPosition().getX() - 2 * radiusBox,
                                babylonBoxItemMock.getPosition().getY() - 2 * radiusBox,
                                4 * radiusBox,
                                4 * radiusBox);
                    }
                });
                // Box
                getTestUiServiceDagger().boxUiService().getBoxes().forEach((integer, syncBoxItemSimpleDto) -> {
                    getGc().setFill(Color.LIGHTGREEN);
                    getGc().fillOval(syncBoxItemSimpleDto.getPosition().getX() - radiusBox,
                            syncBoxItemSimpleDto.getPosition().getY() - radiusBox,
                            2 * radiusBox,
                            2 * radiusBox);

                });
                // Base item marker
                babylonRendererServiceAccessMock.getBabylonBaseItemMocks().forEach(babylonBaseItemMock -> {
                    if (babylonBaseItemMock.getMarkerConfig() != null) {
                        double radius = babylonBaseItemMock.getBaseItemType().getPhysicalAreaConfig().getRadius();
                        getGc().setFill(Color.LIGHTBLUE);
                        getGc().fillOval(babylonBaseItemMock.getPosition().getX() - 2 * radius,
                                babylonBaseItemMock.getPosition().getY() - 2 * radius,
                                4 * radius,
                                4 * radius);
                    }
                });
                // Base item
                babylonRendererServiceAccessMock.getBabylonBaseItemMocks().forEach(babylonBaseItemMock -> {
                    double radius = babylonBaseItemMock.getBaseItemType().getPhysicalAreaConfig().getRadius();
                    getGc().setFill(Color.BLUE);
                    getGc().fillOval(babylonBaseItemMock.getPosition().getX() - radius,
                            babylonBaseItemMock.getPosition().getY() - radius,
                            2 * radius,
                            2 * radius);

                });
                // ViewField
                ViewField viewField = getTestUiServiceDagger().resourceUiService().getViewField();
                if (viewField != null) {
                    strokePolygon(viewField.toList(), 1, Color.BLACK, false);
                }
            }
        }, customRender);
    }

}
