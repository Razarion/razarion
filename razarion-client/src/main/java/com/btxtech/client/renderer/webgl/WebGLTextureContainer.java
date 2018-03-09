package com.btxtech.client.renderer.webgl;

import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.google.gwt.dom.client.ImageElement;
import elemental.client.Browser;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Created by Beat
 * 25.01.2017.
 */
@ApplicationScoped
public class WebGLTextureContainer {
    // private Logger logger = Logger.getLogger(WebGLTextureContainer.class.getName());
    private static final int TERRAIN_MARKER_LENGTH = 512;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ImageUiService imageUiService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private Map<Integer, WebGLTexture> textures = new HashMap<>();
    private Map<Integer, WebGLTexture> bumpTextures = new HashMap<>();
    private WebGLTexture terrainMarkerTexture;

    @PostConstruct
    public void postConstruct() {
        terrainMarkerTexture = gameCanvas.getCtx3d().createTexture();
        inGameQuestVisualizationService.addPlaceConfigCallback(() -> {
            if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
                setupTerrainMarkerTexture(inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfig(), inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundaryRect());
            } else {
                setupEmptyTerrainMarkerTexture();
            }
        });
    }

    public void setupTextures() {
        for (Integer imageId : gameUiControl.getAllTextureIds()) {
            imageUiService.requestImage(imageId, imageElement -> setupTexture(imageId, imageElement));
        }
        for (Integer imageId : gameUiControl.getAllBumpTextureIds()) {
            imageUiService.requestImage(imageId, imageElement -> setupTextureForBumpMap(imageId, imageElement));
        }
        setupEmptyTerrainMarkerTexture();
    }

    public WebGLTexture getTexture(int imageId) {
        WebGLTexture webGLTexture = textures.get(imageId);
        if (webGLTexture != null) {
            return webGLTexture;
        }
        webGLTexture = gameCanvas.getCtx3d().createTexture();
        WebGLTexture finalWebGLTexture = webGLTexture;
        imageUiService.requestImage(imageId, imageElement -> bindTexture(imageElement, finalWebGLTexture));
        return webGLTexture;
    }

    public WebGLTexture getTextureForBumpMap(int imageId) {
        WebGLTexture webGLTexture = bumpTextures.get(imageId);
        if (webGLTexture != null) {
            return webGLTexture;
        }
        webGLTexture = gameCanvas.getCtx3d().createTexture();
        WebGLTexture finalWebGLTexture = webGLTexture;
        imageUiService.requestImage(imageId, imageElement -> bindTextureForBumpMap(imageElement, finalWebGLTexture));
        return webGLTexture;
    }

    public void handleImageSize(int imageId, IntConsumer pixelSizeConsumer) {
        imageUiService.requestImage(imageId, imageElement -> pixelSizeConsumer.accept(imageElement.getWidth()));
    }

    private void setupTexture(int imageId, ImageElement imageElement) {
        WebGLTexture webGLTexture = textures.get(imageId);
        if (webGLTexture == null) {
            webGLTexture = gameCanvas.getCtx3d().createTexture();
            textures.put(imageId, webGLTexture);
        }
        bindTexture(imageElement, webGLTexture);
    }

    protected void setupTextureForBumpMap(int imageId, ImageElement imageElement) {
        WebGLTexture webGLTexture = bumpTextures.get(imageId);
        if (webGLTexture == null) {
            webGLTexture = gameCanvas.getCtx3d().createTexture();
            bumpTextures.put(imageId, webGLTexture);
        }
        bindTextureForBumpMap(imageElement, webGLTexture);
    }

    private void bindTexture(ImageElement imageElement, WebGLTexture webGLTexture) {
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR_MIPMAP_NEAREST);
        gameCanvas.getCtx3d().generateMipmap(WebGLRenderingContext.TEXTURE_2D);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }

    private void bindTextureForBumpMap(ImageElement imageElement, WebGLTexture webGLTexture) {
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }

    private void setupEmptyTerrainMarkerTexture() {
        try {
            // Create canvas image
            CanvasElement canvasElement = Browser.getDocument().createCanvasElement();
            canvasElement.setWidth(TERRAIN_MARKER_LENGTH);
            canvasElement.setHeight(TERRAIN_MARKER_LENGTH);
            bindTextureTerrainMarker(canvasElement);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void setupTerrainMarkerTexture(PlaceConfig placeConfig, Rectangle2D placeConfigBoundary) {
        try {
            // Create canvas image
            CanvasElement canvasElement = Browser.getDocument().createCanvasElement();
            canvasElement.setWidth(TERRAIN_MARKER_LENGTH);
            canvasElement.setHeight(TERRAIN_MARKER_LENGTH);
            CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvasElement.getContext("2d");
            ctx.setFillStyle("#000000");
            ctx.fillRect(0, 0, TERRAIN_MARKER_LENGTH, TERRAIN_MARKER_LENGTH);

            ctx.setFillStyle("#FFFFFF");
            if (placeConfig.getPosition() != null && placeConfig.getRadius() != null) {
                ctx.beginPath();
                ctx.arc(TERRAIN_MARKER_LENGTH / 2, TERRAIN_MARKER_LENGTH / 2, TERRAIN_MARKER_LENGTH / 2, 0, (float) (2.0 * Math.PI), false);
                ctx.fill();
            } else if (placeConfig.getPolygon2D() != null) {
                ctx.save();
                float xScale = (float) ((double) TERRAIN_MARKER_LENGTH / placeConfigBoundary.width());
                float yScale = (float) -((double) TERRAIN_MARKER_LENGTH / placeConfigBoundary.height());
                ctx.scale(xScale, yScale);
                ctx.translate((float) -placeConfigBoundary.startX(), (float) (-placeConfigBoundary.startY() - placeConfigBoundary.height()));
                ctx.beginPath();
                for (int i = 0; i < placeConfig.getPolygon2D().getCorners().size(); i++) {
                    DecimalPosition corner = placeConfig.getPolygon2D().getCorners().get(i);
                    if (i == 0) {
                        ctx.moveTo((float) corner.getX(), (float) corner.getY());
                    } else {
                        ctx.lineTo((float) corner.getX(), (float) corner.getY());
                    }
                }
                ctx.closePath();
                ctx.fill();
                ctx.restore();
            }
//            ///////////
//            canvasElement.getStyle().setZIndex(1000);
//            canvasElement.getStyle().setPosition("absolute");
//            canvasElement.getStyle().setLeft("0");
//            canvasElement.getStyle().setTop("0");
//            Browser.getDocument().getBody().appendChild(canvasElement);
//            ///////////
            bindTextureTerrainMarker(canvasElement);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void bindTextureTerrainMarker(CanvasElement canvasElement) {
        // Bind to texture
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, terrainMarkerTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, canvasElement);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }

    public WebGLTexture getTerrainMarkerTexture() {
        return terrainMarkerTexture;
    }
}
