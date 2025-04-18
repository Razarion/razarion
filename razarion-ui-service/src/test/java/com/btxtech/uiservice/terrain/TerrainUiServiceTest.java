package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.gui.userobject.MouseMoveRender;
import com.btxtech.uiservice.renderer.ViewField;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import org.junit.Test;

import static com.btxtech.shared.gameengine.planet.terrain.container.TerrainType.LAND;
import static com.btxtech.shared.gameengine.planet.terrain.container.TerrainType.WATER;

/**
 * Created by Beat
 * 25.12.2015.
 */
public class TerrainUiServiceTest extends DaggerUiBaseIntegrationTest {

    @Test
    public void getTerrainType() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        setupUiEnvironment(coldGameUiContext);
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(new DecimalPosition(10, 0));
        viewField.setBottomRight(new DecimalPosition(40, 0));
        viewField.setTopRight(new DecimalPosition(50, 50));
        viewField.setTopLeft(new DecimalPosition(0, 50));
        getTestUiServiceDagger().terrainUiService().onViewChanged(viewField, viewField.calculateAabbRectangle());
        // ---------------------

        showDisplay(new MouseMoveRender() {
            @Override
            protected void renderMouse(GraphicsContext gc, DecimalPosition position) {
                TerrainType terrainType = getTestUiServiceDagger().terrainUiService().getTerrainType(position);
                switch (terrainType) {
                    case WATER:
                        gc.setFill(Color.BLUE);
                        break;
                    case LAND:
                        gc.setFill(Color.GREEN);
                        break;
                    case BLOCKED:
                        gc.setFill(Color.RED);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown terrain type: " + terrainType);
                }
                gc.fillArc(position.getX() - 1, position.getY() - 1, 2, 2, 0, 360, ArcType.ROUND);
            }
        });
    }

    @Test
    public void isTerrainFree() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        setupUiEnvironment(coldGameUiContext);
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(new DecimalPosition(10, 0));
        viewField.setBottomRight(new DecimalPosition(40, 0));
        viewField.setTopRight(new DecimalPosition(50, 50));
        viewField.setTopLeft(new DecimalPosition(0, 50));
        getTestUiServiceDagger().terrainUiService().onViewChanged(viewField, viewField.calculateAabbRectangle());
        // ---------------------

        showDisplay(new MouseMoveRender() {
            @Override
            protected void renderMouse(GraphicsContext gc, DecimalPosition position) {
                boolean free = getTestUiServiceDagger().terrainUiService().isTerrainFree(position, 10, LAND);
                if (free) {
                    gc.setFill(Color.GREEN);
                } else {
                    gc.setFill(Color.RED);
                }
                gc.fillArc(position.getX() - 10, position.getY() - 10, 20, 20, 0, 360, ArcType.ROUND);
            }
        });
    }
}