package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.events.MouseEvent;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 16.06.2017.
 */
@Templated("RadarPanel.html#radar")
public class RadarPanel extends Composite implements ViewService.ViewFieldListener {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;
    public static final int MINI_MAP_IMAGE_WIDTH = 1000;
    public static final int MINI_MAP_IMAGE_HEIGHT = 1000;
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
    @Inject
    @DataField
    private Button leftButton;
    @Inject
    @DataField
    private Button rightButton;
    @Inject
    @DataField
    private Button downButton;
    @Inject
    @DataField
    private Button upButton;
    @Inject
    @DataField
    private Button zoomInButton;
    @Inject
    @DataField
    private Button zoomOuButton;

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
        DecimalPosition viewCenter = miniViewField.canvasToReal(new DecimalPosition(mouseEvent.getOffsetX(), mouseEvent.getOffsetY()));
        DecimalPosition cameraPosition = projectionTransformation.viewFieldCenterToCamera(viewCenter, 0);
        camera.setTranslateXY(cameraPosition.getX(), cameraPosition.getY());
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        miniViewField.setViewField(viewField);
        miniTerrain.setViewField(viewField);

        miniTerrain.update();
        miniViewField.update();
    }
}
