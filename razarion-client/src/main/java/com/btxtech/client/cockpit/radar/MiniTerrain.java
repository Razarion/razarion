package com.btxtech.client.cockpit.radar;

import com.btxtech.client.imageservice.ImageLoader;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import elemental.html.CanvasRenderingContext2D;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 19.08.2017.
 */
@Dependent
public class MiniTerrain extends AbstractMiniMap {
    private Logger logger = Logger.getLogger(MiniTerrain.class.getName());
    @Inject
    private GameUiControl gameUiControl;
    private ImageElement imageElement;
    private Runnable imageLoaderCallback;

    public void init(Element canvasElement, int width, int height, double zoom, Runnable imageLoaderCallback) {
        this.imageLoaderCallback = imageLoaderCallback;
        super.init(canvasElement, width, height, zoom);
    }

    public void show() {
        ImageLoader<Integer> imageLoader = new ImageLoader<>();
        imageLoader.addImageUrl(CommonUrl.getMiniMapPlanetUrl(gameUiControl.getPlanetConfig().getId()), gameUiControl.getPlanetConfig().getId());
        imageLoader.startLoading((loadedImageElements, failed) -> {
            if (!failed.isEmpty()) {
                logger.warning("MiniTerrain.postConstruct() loading image failed: " + failed);
                return;
            }
            ImageElement imageElement = loadedImageElements.get(gameUiControl.getPlanetConfig().getId());
            if (imageElement == null) {
                logger.warning("MiniTerrain.postConstruct() loading not loaded: " + failed);
                return;
            }
            MiniTerrain.this.imageElement = imageElement;
            imageLoaderCallback.run();
        });
    }

    @Override
    protected void setupTransformation(double zoom, CanvasRenderingContext2D ctx, int width, int height) {
        double imageScale = (float) Math.min((double) width / RadarPanel.MINI_MAP_IMAGE_WIDTH, (double) height / RadarPanel.MINI_MAP_IMAGE_HEIGHT);
        imageScale *= zoom;
        ctx.scale((float) imageScale, (float) imageScale);
        double gameScale = setupGameScale();
        DecimalPosition centerOffset = getViewField().calculateCenter().sub(gameUiControl.getPlanetConfig().getPlayGround().getStart()).divide(imageScale / gameScale);

        float xDownerLimit = (float) (width / imageScale / 2.0);
        float xUpperLimit = RadarPanel.MINI_MAP_IMAGE_WIDTH - xDownerLimit;
        float xShift;
        if (centerOffset.getX() < xDownerLimit) {
            xShift = xDownerLimit;
        } else if (centerOffset.getX() > xUpperLimit) {
            xShift = xUpperLimit;
        } else {
            xShift = (float) centerOffset.getX();
        }

        float yDownerLimit = (float) (height / imageScale / 2.0);
        float yUpperLimit = RadarPanel.MINI_MAP_IMAGE_HEIGHT - yDownerLimit;
        float yShift;
        if (centerOffset.getY() < yDownerLimit) {
            yShift = yDownerLimit;
        } else if (centerOffset.getY() > yUpperLimit) {
            yShift = yUpperLimit;
        } else {
            yShift = (float) centerOffset.getY();
        }

        ctx.translate(xDownerLimit - xShift, yShift - yUpperLimit);
    }

    @Override
    protected void draw(CanvasRenderingContext2D ctx) {
        if (imageElement == null) {
            return;
        }
        getCtx().drawImage(GwtUtils.castImageElement(imageElement), 0, 0);
    }
}
