package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.cdimock.BabylonRendererServiceAccessMock;
import com.btxtech.uiservice.cdimock.TestItemCockpitFrontend;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.gui.AbstractUiTestGuiRenderer;
import com.btxtech.uiservice.gui.UiTestGuiDisplay;
import com.btxtech.uiservice.i18n.I18nConstants;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;
import javafx.scene.paint.Color;
import org.easymock.EasyMock;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class WeldUiBaseIntegrationTest {
    // ... work in progress ...
    private WeldContainer weldContainer;
    private final Logger LOG = Logger.getLogger(WeldUiBaseIntegrationTest.class.getName());

    protected void setupUiEnvironment(ColdGameUiContext coldGameUiContext) {
        // Init weld
        Weld weld = new Weld();
        weldContainer = weld.initialize();

        weldContainer.getBeanManager().fireEvent(new GameUiControlInitEvent(coldGameUiContext));
    }

    protected void setupUiEnvironment(PlanetVisualConfig planetVisualConfig) {
        setupUiEnvironment(new ColdGameUiContext().warmGameUiContext(new WarmGameUiContext().setPlanetVisualConfig(planetVisualConfig)));
    }

    protected <T> T getWeldBean(Class<T> clazz) {
        return weldContainer.instance().select(clazz).get();
    }

    protected void callOnViewChanged(ViewField viewField) {
        getWeldBean(BaseItemUiService.class).onViewChanged(viewField, viewField.calculateAabbRectangle());
    }

    protected TerrainUiService getTerrainUiService() {
        return getWeldBean(TerrainUiService.class);
    }

    protected BabylonRendererServiceAccessMock getBabylonRendererServiceAccessMock() {
        return getWeldBean(BabylonRendererServiceAccessMock.class);
    }

    protected void setupAlarmService() {
        AlarmService alarmService = getWeldBean(AlarmService.class);
        alarmService.addListener(alarm -> LOG.severe(alarm.toString()));
        alarmService.getAlarms().forEach(alarm -> LOG.severe(alarm.toString()));
    }

    protected void setupI18nConstants() {
        I18nConstants i18nConstants = EasyMock.createNiceMock(I18nConstants.class);
        EasyMock.replay(i18nConstants);
        I18nHelper.setConstants(i18nConstants);
    }

    protected void createBase(int userId, int baseId) {
        getWeldBean(BaseItemUiService.class).addBase(new PlayerBaseDto()
                .name("Test Base")
                .baseId(baseId)
                .userId(userId)
                .character(Character.HUMAN));
    }

    protected SyncResourceItemSimpleDto createResource(int id, int itemTypeId, Vertex position3d) {
        SyncResourceItemSimpleDto syncResourceItemSimpleDto = new SyncResourceItemSimpleDto();
        syncResourceItemSimpleDto.setId(id);
        syncResourceItemSimpleDto.setItemTypeId(itemTypeId);
        syncResourceItemSimpleDto.setPosition2d(position3d.toXY());
        syncResourceItemSimpleDto.setPosition3d(position3d);
        return syncResourceItemSimpleDto;
    }

    protected SyncBoxItemSimpleDto createBox(int id, int itemTypeId, Vertex position3d) {
        SyncBoxItemSimpleDto syncBoxItemSimpleDto = new SyncBoxItemSimpleDto();
        syncBoxItemSimpleDto.setId(id);
        syncBoxItemSimpleDto.setItemTypeId(itemTypeId);
        syncBoxItemSimpleDto.setPosition2d(position3d.toXY());
        syncBoxItemSimpleDto.setPosition3d(position3d);
        return syncBoxItemSimpleDto;
    }

    protected BabylonRendererServiceAccessMock.BabylonBaseItemMock findBaseItem(int id) {
        BabylonRendererServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        for (BabylonRendererServiceAccessMock.BabylonBaseItemMock babylonBaseItemMock : babylonRendererServiceAccessMock.getBabylonBaseItemMocks()) {
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
            public void show(boolean admin) {

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

    public void display() {
        BabylonRendererServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        ResourceItemType resourceItemType = getWeldBean(ItemTypeService.class).getResourceItemType(FallbackConfig.RESOURCE_ITEM_TYPE_ID);
        double radiusResource = resourceItemType.getRadius();
        BoxItemType boxItemType = getWeldBean(ItemTypeService.class).getBoxItemType(FallbackConfig.BOX_ITEM_TYPE_ID);
        double radiusBox = boxItemType.getRadius();
        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
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
                getWeldBean(ResourceUiService.class).getResources().forEach((integer, syncResourceItemSimpleDto) -> {
                    getGc().setFill(Color.PINK);
                    getGc().fillOval(syncResourceItemSimpleDto.getPosition2d().getX() - radiusResource,
                            syncResourceItemSimpleDto.getPosition2d().getY() - radiusResource,
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
                getWeldBean(BoxUiService.class).getBoxes().forEach((integer, syncBoxItemSimpleDto) -> {
                    getGc().setFill(Color.LIGHTGREEN);
                    getGc().fillOval(syncBoxItemSimpleDto.getPosition2d().getX() - radiusBox,
                            syncBoxItemSimpleDto.getPosition2d().getY() - radiusBox,
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

                // Field
                ViewField viewField = getWeldBean(ResourceUiService.class).getViewField();
                if (viewField != null) {
                    strokePolygon(viewField.toList(), 1, Color.BLACK, false);
                }
            }
        });
    }

}
