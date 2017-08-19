package com.btxtech.client.editor.terrain;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.utils.MathHelper;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 16.06.2017.
 */
@Templated("MiniMapDialog.html#miniMapDialog")
public class MiniMapDialog extends Composite implements ModalDialogContent<Void> {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    private static final String WATER_COLOR = "#0000ff";
    private static final String GROUND_COLOR = "#86b300";
    private static final String SLOPE_COLOR = "#8c8c8c";
    private static final String TERRAIN_OBJECT_COLOR = "#008000";
    private Logger logger = Logger.getLogger(MiniMapDialog.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainEditorImpl terrainEditor;
    @DataField
    private Element miniMapElement = (Element) Browser.getDocument().createCanvasElement();
    @DataField
    @Inject
    private Button saveMiniMapButton;
    private CanvasElement canvasElement;
    private CanvasRenderingContext2D ctx;

    @Override
    public void init(Void aVoid) {
        canvasElement = (CanvasElement) miniMapElement;
        this.canvasElement.setWidth(WIDTH);
        this.canvasElement.setHeight(HEIGHT);
        ctx = (CanvasRenderingContext2D) this.canvasElement.getContext("2d");
        generateMiniTerrain();
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {

    }

    @Override
    public void onClose() {

    }

    @EventHandler("saveMiniMapButton")
    private void saveMiniMapButton(ClickEvent event) {
        String dataUrl =  canvasElement.toDataURL("image/jpeg");
        terrainEditor.saveMiniMapImage(dataUrl);
    }

    private void generateMiniTerrain() {
        ctx.save();

        Rectangle2D playground = terrainEditor.getPlanetConfig().getPlayGround();
        float scale = (float) Math.min((double) WIDTH / playground.width(), (double) HEIGHT / playground.height());
        ctx.translate(0, HEIGHT);
        ctx.scale(scale, -scale);

        // Ground
        ctx.setFillStyle(GROUND_COLOR);
        ctx.beginPath();
        ctx.rect(0, 0, (float) playground.width(), (float) playground.height());
        ctx.fill();

        // Slopes
        for (TerrainSlopePosition terrainSlopePosition : terrainEditor.generateTerrainSlopePositions()) {
            SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigId());
            switch (slopeSkeletonConfig.getType()) {
                case LAND:
                    drawPlateau(playground, terrainSlopePosition, slopeSkeletonConfig);
                    break;
                case WATER:
                    drawWater(playground, terrainSlopePosition);
                    break;
                default:
                    logger.warning("MiniMapDialog.generateMiniTerrain() unknown slopeSkeletonConfig.getType(): " + slopeSkeletonConfig.getType());
            }
        }

        // Terrain objects
        ctx.setFillStyle(TERRAIN_OBJECT_COLOR);
        for (TerrainObjectPosition terrainObjectPosition : terrainEditor.generateTerrainObjectPositions()) {
            TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectId());
            DecimalPosition center = terrainObjectPosition.getPosition().sub(playground.getStart());
            ctx.beginPath();
            ctx.arc((float) center.getX(), (float) center.getY(), (float) terrainObjectConfig.getRadius(), 0f, (float) MathHelper.ONE_RADIANT, true);
            ctx.fill();
        }
        ctx.restore();
    }

    private void drawPlateau(Rectangle2D playground, TerrainSlopePosition terrainSlopePosition, SlopeSkeletonConfig slopeSkeletonConfig) {
        ctx.setStrokeStyle(SLOPE_COLOR);
        ctx.setLineWidth((float) slopeSkeletonConfig.getWidth());

        ctx.beginPath();
        List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition position = polygon.get(i).getPosition().sub(playground.getStart());
            if (i == 0) {
                ctx.moveTo((float) position.getX(), (float) position.getY());
            } else {
                ctx.lineTo((float) position.getX(), (float) position.getY());
            }
        }
        ctx.stroke();
    }

    private void drawWater(Rectangle2D playground, TerrainSlopePosition terrainSlopePosition) {
        ctx.setFillStyle(WATER_COLOR);

        ctx.beginPath();
        List<TerrainSlopeCorner> polygon = terrainSlopePosition.getPolygon();
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition position = polygon.get(i).getPosition().sub(playground.getStart());
            if (i == 0) {
                ctx.moveTo((float) position.getX(), (float) position.getY());
            } else {
                ctx.lineTo((float) position.getX(), (float) position.getY());
            }
        }
        ctx.closePath();
        ctx.fill();

    }
}
