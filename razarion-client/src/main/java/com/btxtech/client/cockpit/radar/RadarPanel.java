package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.events.MouseEvent;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 16.06.2017.
 */
@Templated("RadarPanel.html#radar")
public class RadarPanel extends Composite implements ViewService.ViewFieldListener {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    // private Logger logger = Logger.getLogger(RadarPanel.class.getName());
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ViewService viewService;
    @Inject
    private Camera camera;
    @DataField
    private Element miniTerrainElement = (Element) Browser.getDocument().createCanvasElement();
    @DataField
    private Element miniViewFiledElement = (Element) Browser.getDocument().createCanvasElement();
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    @DataField
    private Div miniMap;
    @Inject
    private MiniTerrain miniTerrain;
    @Inject
    private MiniViewField miniViewField;

    @PostConstruct
    public void postConstruct() {
        miniTerrain.init(miniTerrainElement, WIDTH, HEIGHT);
        miniViewField.init(miniViewFiledElement, WIDTH, HEIGHT);
        viewService.addViewFieldListeners(this);
        setSize(WIDTH + "px", HEIGHT + "px");
        miniMap.getStyle().setProperty("width", WIDTH + "px");
        miniMap.getStyle().setProperty("height", HEIGHT + "px");
        miniMap.addEventListener("mousedown", event -> onMouseDown((MouseEvent) event), false);
    }

    private void onMouseDown(MouseEvent mouseEvent) {
        DecimalPosition viewCenter = new DecimalPosition(mouseEvent.getOffsetX(), HEIGHT - mouseEvent.getOffsetY()).divide(miniTerrain.calculateMinScale(gameUiControl.getPlanetConfig().getPlayGround()));
        DecimalPosition cameraPosition = projectionTransformation.viewFieldCenterToCamera(viewCenter, 0);
        camera.setTranslateXY(cameraPosition.getX(), cameraPosition.getY());
    }

    public void generateMiniTerrain(List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        miniTerrain.generateMiniTerrain(gameUiControl.getPlanetConfig().getPlayGround(), terrainSlopePositions, terrainObjectPositions);
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        miniViewField.onViewChanged(viewField, gameUiControl.getPlanetConfig().getPlayGround());
    }

    public String toDataURL(String type) {
        return miniTerrain.toDataURL(type);
    }
}
