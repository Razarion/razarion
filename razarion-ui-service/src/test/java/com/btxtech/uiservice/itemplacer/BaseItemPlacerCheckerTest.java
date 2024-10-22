package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.DaggerUiBaseIntegrationTest;
import com.btxtech.uiservice.gui.userobject.MouseMoveRender;
import com.btxtech.uiservice.renderer.ViewField;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import org.junit.Test;

public class BaseItemPlacerCheckerTest extends DaggerUiBaseIntegrationTest {
    @Test
    public void onWater() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        setupUiEnvironment(coldGameUiContext);
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(new DecimalPosition(10, 0));
        viewField.setBottomRight(new DecimalPosition(40, 0));
        viewField.setTopRight(new DecimalPosition(50, 50));
        viewField.setTopLeft(new DecimalPosition(0, 50));
        getTestUiServiceDagger().terrainUiService().onViewChanged(viewField, viewField.calculateAabbRectangle());
        // ---------------------


        BaseItemPlacerChecker baseItemPlacerChecker = getTestUiServiceDagger().baseItemPlacerChecker();

        BaseItemType harbour = getTestUiServiceDagger().itemTypeService().getBaseItemType(FallbackConfig.HARBOUR_ITEM_TYPE_ID);

        baseItemPlacerChecker.init(harbour, new BaseItemPlacerConfig()
                .baseItemCount(1));
        baseItemPlacerChecker.check(new DecimalPosition(6, 6));

        System.out.println("isAllowedAreaOk: " + baseItemPlacerChecker.isAllowedAreaOk());
        System.out.println("isTerrainOk: " + baseItemPlacerChecker.isTerrainOk());
        System.out.println("isItemsOk: " + baseItemPlacerChecker.isItemsOk());
        System.out.println("isEnemiesOk: " + baseItemPlacerChecker.isEnemiesOk());
        System.out.println("isPositionValid: " + baseItemPlacerChecker.isPositionValid());


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
                        gc.setFill(Color.BLUE);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown terrain type: " + terrainType);
                }
                gc.fillArc(position.getX() - 5, position.getY() - 5, 10, 10, 0, 360, ArcType.ROUND);
            }
        });

    }

}