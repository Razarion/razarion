package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class WeldUiBaseTest {
    // ... work in progress ...
    private WeldContainer weldContainer;

    protected void setupUiEnvironment(PlanetVisualConfig planetVisualConfig) {
        // Init weld
        Weld weld = new Weld();
        weldContainer = weld.initialize();

        weldContainer.getBeanManager().fireEvent(new GameUiControlInitEvent(new ColdGameUiContext().setWarmGameUiContext(new WarmGameUiContext().setPlanetVisualConfig(planetVisualConfig))));

        // getWeldBean(Event.class).fire(new WeldUiBaseTest());
    }

    protected <T> T getWeldBean(Class<T> clazz) {
        return weldContainer.instance().select(clazz).get();
    }

    protected void setCamera(double translateX, double translateY) {
        Camera camera = getWeldBean(Camera.class);
        camera.setTranslateXY(translateX, translateY);
    }

    protected void setCamera(double translateX, double translateY, double rotateX) {
        Camera camera = getWeldBean(Camera.class);
        camera.setTranslateXY(translateX, translateY);
        camera.setRotateX(rotateX);
    }

    protected void setCamera(double translateX, double translateY, double translateZ, double rotateX, double rotateZ) {
        Camera camera = getWeldBean(Camera.class);
        camera.setTranslateX(translateX);
        camera.setTranslateY(translateY);
        camera.setTranslateZ(translateZ);
        camera.setRotateX(rotateX);
        camera.setRotateZ(rotateZ);
    }

    protected ProjectionTransformation getProjectionTransformation() {
        return getWeldBean(ProjectionTransformation.class);
    }

    protected TerrainUiService getTerrainUiService() {
        return getWeldBean(TerrainUiService.class);
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
