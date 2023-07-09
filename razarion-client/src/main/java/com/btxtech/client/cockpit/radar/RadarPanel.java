package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.events.MouseEvent;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 16.06.2017.
 */
@Templated("RadarPanel.html#radar")
public class RadarPanel extends Composite implements ViewService.ViewFieldListener {
    public static final int WIDTH = 200;
    public static final int HEIGHT = 200;
    public static final int MINI_MAP_IMAGE_WIDTH = 1000;
    public static final int MINI_MAP_IMAGE_HEIGHT = 1000;
    private static final int MAX_ZOOM = 10;
    private static final int DEFAULT_ZOOM = 3;
    // private Logger logger = Logger.getLogger(RadarPanel.class.getName());
    @Inject
    private ViewService viewService;
    @DataField
    private Element miniTerrainElement = (Element) Browser.getDocument().createCanvasElement();
    @DataField
    private Element miniViewFieldElement = (Element) Browser.getDocument().createCanvasElement();
    @DataField
    private Element miniItemViewElement = (Element) Browser.getDocument().createCanvasElement();
    @Inject
    @DataField
    private Div miniMap;
    @Inject
    private MiniTerrain miniTerrain;
    @Inject
    private MiniViewField miniViewField;
    @Inject
    private MiniItemView miniItemView ;
    @Inject
    @DataField
    private Button zoomInButton;
    @Inject
    @DataField
    private Button zoomOuButton;
    private int zoom = DEFAULT_ZOOM;

    @PostConstruct
    public void postConstruct() {
        miniTerrain.init(miniTerrainElement, WIDTH, HEIGHT, zoom, this::updateMiniMap);
        miniViewField.init(miniViewFieldElement, WIDTH, HEIGHT, zoom);
        miniItemView.init(miniItemViewElement, WIDTH, HEIGHT, zoom);
        viewService.addViewFieldListeners(this);
        miniMap.getStyle().setProperty("width", WIDTH + "px");
        miniMap.getStyle().setProperty("height", HEIGHT + "px");
        miniMap.addEventListener("mousedown", event -> onMouseDown((MouseEvent) event), false);
    }

    public void show() {
        miniTerrain.show();
        miniItemView.startUpdater();
    }

    public void stop() {
        miniItemView.stopUpdater();
    }

    private void onMouseDown(MouseEvent mouseEvent) {
        DecimalPosition viewCenter = miniViewField.canvasToReal(new DecimalPosition(mouseEvent.getOffsetX(), mouseEvent.getOffsetY()));
        // TODO DecimalPosition cameraPosition = projectionTransformation.viewFieldCenterToCamera(viewCenter, 0);
        // TODO camera.setTranslateXY(cameraPosition.getX(), cameraPosition.getY());
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        miniViewField.setViewField(viewField);
        miniTerrain.setViewField(viewField);
        miniItemView.setViewField(viewField);

        updateMiniMap();
    }

    @EventHandler("zoomInButton")
    private void zoomInButtonClick(ClickEvent event) {
        zoom++;
        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        }
        changeZoom();
    }

    @EventHandler("zoomOuButton")
    private void zoomOuButtonClick(ClickEvent event) {
        zoom--;
        if (zoom < 1) {
            zoom = 1;
        }
        changeZoom();
    }

    private void changeZoom() {
        miniTerrain.setZoom(zoom);
        miniViewField.setZoom(zoom);
        miniItemView.setZoom(zoom);
        updateMiniMap();
    }

    private void updateMiniMap() {
        miniTerrain.update();
        miniViewField.update();
        miniItemView.update();
    }
}