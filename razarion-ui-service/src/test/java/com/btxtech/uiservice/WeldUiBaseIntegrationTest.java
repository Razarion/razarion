package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.cdimock.ThreeJsRendererServiceAccessMock;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.i18n.I18nConstants;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
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
        getWeldBean(BaseItemUiService.class).onViewChanged(viewField);
    }

    @Deprecated// See callOnViewChanged
    protected void setCamera(double translateX, double translateY) {
        Camera camera = getWeldBean(Camera.class);
        camera.setTranslateXY(translateX, translateY);
    }

    @Deprecated// See callOnViewChanged
    protected void setCamera(double translateX, double translateY, double rotateX) {
        Camera camera = getWeldBean(Camera.class);
        camera.setTranslateXY(translateX, translateY);
        camera.setRotateX(rotateX);
        getWeldBean(ViewService.class).onViewChanged();
    }

    @Deprecated// See callOnViewChanged
    protected void setCamera(double translateX, double translateY, double translateZ, double rotateX, double rotateZ) {
        Camera camera = getWeldBean(Camera.class);
        camera.setTranslateX(translateX);
        camera.setTranslateY(translateY);
        camera.setTranslateZ(translateZ);
        camera.setRotateX(rotateX);
        camera.setRotateZ(rotateZ);
    }

    @Deprecated// See callOnViewChanged
    protected ProjectionTransformation getProjectionTransformation() {
        return getWeldBean(ProjectionTransformation.class);
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
