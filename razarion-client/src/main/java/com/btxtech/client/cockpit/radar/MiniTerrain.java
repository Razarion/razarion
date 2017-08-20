package com.btxtech.client.cockpit.radar;

import com.btxtech.client.imageservice.ImageLoader;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.dom.client.ImageElement;
import elemental.html.CanvasRenderingContext2D;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void postConstruct() {
        ImageLoader<Integer> imageLoader = new ImageLoader<>();
        imageLoader.addImageUrl(RestUrl.getMiniMapPlanetUrl(gameUiControl.getPlanetConfig().getPlanetId()), gameUiControl.getPlanetConfig().getPlanetId());
        imageLoader.startLoading((loadedImageElements, failed) -> {
            if (!failed.isEmpty()) {
                logger.warning("MiniTerrain.postConstruct() loading image failed: " + failed);
                return;
            }
            ImageElement imageElement = loadedImageElements.get(gameUiControl.getPlanetConfig().getPlanetId());
            if (imageElement == null) {
                logger.warning("MiniTerrain.postConstruct() loading not loaded: " + failed);
                return;
            }
            MiniTerrain.this.imageElement = imageElement;
            // TODO drawImage();
        });
    }

    @Override
    protected void setupTransformation(ScaleStep scaleStep, CanvasRenderingContext2D ctx, int width, int height) {
        if (scaleStep == ScaleStep.WHOLE_MAP) {
            double scale = (float) Math.min((double) width / RadarPanel.MINI_MAP_IMAGE_WIDTH, (double) height / RadarPanel.MINI_MAP_IMAGE_HEIGHT);
            ctx.scale((float) scale, (float) scale);
            // ctx.translate(0, 0);
        } else {
            throw new IllegalArgumentException("AbstractMiniMap.setScaleStep(): " + scaleStep);
        }
    }

    @Override
    protected void draw(CanvasRenderingContext2D ctx) {
        if (imageElement == null) {
            return;
        }
        getCtx().drawImage(GwtUtils.castImageElement(imageElement), 0, 0);
    }
}
