package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.cdimock.TestItemCockpitFrontend;
import com.btxtech.uiservice.cdimock.ThreeJsRendererServiceAccessMock;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.i18n.I18nConstants;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;
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

    protected ThreeJsRendererServiceAccessMock getThreeJsRendererServiceAccessMock() {
        return getWeldBean(ThreeJsRendererServiceAccessMock.class);
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

    protected Vertex toNdcVertex(Vertex4 vertex4) {
        double ndcX = vertex4.getX() / vertex4.getW();
        double ndcY = vertex4.getY() / vertex4.getW();
        double ndcZ = vertex4.getZ() / vertex4.getW();

//        if (ndcX > 1 || ndcX < -1) {
//            return null;
//        }
//        if (ndcY > 1 || ndcY < -1) {
//            return null;
//        }
//        if (ndcZ > 1 || ndcZ < -1) {
//            return null;
//        }

        return new Vertex(ndcX, ndcY, ndcZ);
    }

}
