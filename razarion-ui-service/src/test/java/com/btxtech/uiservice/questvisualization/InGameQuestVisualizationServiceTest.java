package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.BabylonRendererServiceAccessMock;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.gui.AbstractUiTestGuiRenderer;
import com.btxtech.uiservice.gui.UiTestGuiDisplay;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class InGameQuestVisualizationServiceTest extends WeldUiBaseIntegrationTest {
    @Test
    public void activateInsideScrollOutside() {
        ColdGameUiContext coldGameUiContext = setup();

        getWeldBean(ResourceUiService.class).addResource(createResource(1,
                FallbackConfig.RESOURCE_ITEM_TYPE_ID,
                new Vertex(150, 175, 0)));

        createBase(coldGameUiContext.getUserContext().getUserId(), 21);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.HARVEST).comparisonConfig(new ComparisonConfig().count(10))));


        BabylonRendererServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(1, babylonRendererServiceAccessMock.getBabylonResourceItemMocks().size());
        BabylonRendererServiceAccessMock.BabylonResourceItemMock babylonResourceItemMock = babylonRendererServiceAccessMock.getBabylonResourceItemMocks().get(0);
        Assert.assertNotNull(babylonResourceItemMock.getMarkerConfig());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);


        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(270, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonResourceItemMocks().size());
    }

    @Test
    public void activateOutsideScrollInside() {
        ColdGameUiContext coldGameUiContext = setup();

        getWeldBean(ResourceUiService.class).addResource(createResource(1,
                FallbackConfig.RESOURCE_ITEM_TYPE_ID,
                new Vertex(150, 250, 0)));

        createBase(coldGameUiContext.getUserContext().getUserId(), 21);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.HARVEST).comparisonConfig(new ComparisonConfig().count(10))));

        BabylonRendererServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(90, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonResourceItemMocks().size());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);

        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(1, babylonRendererServiceAccessMock.getBabylonResourceItemMocks().size());
        BabylonRendererServiceAccessMock.BabylonResourceItemMock babylonResourceItemMock = babylonRendererServiceAccessMock.getBabylonResourceItemMocks().get(0);
        Assert.assertNotNull(babylonResourceItemMock.getMarkerConfig());
    }

    private ColdGameUiContext setup() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .setUserId(1)
                .setUnlockedItemLimit(Collections.emptyMap())
                .setLevelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();

        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        getWeldBean(TerrainUiService.class).setLoaded();

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 40,
                240, 40,
                300, 200,
                0, 200);

        setupI18nConstants();
        setupCockpit();

        return coldGameUiContext;
    }

    private void display() {
        //---------------------------
        ResourceItemType resourceItemType = getWeldBean(ItemTypeService.class).getResourceItemType(FallbackConfig.RESOURCE_ITEM_TYPE_ID);
        double radius = resourceItemType.getRadius();
        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
                getWeldBean(ResourceUiService.class).getResources().forEach((integer, syncResourceItemSimpleDto) -> {
                    //--------------------------- Marked Resource
                    BabylonRendererServiceAccessMock threeJsRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
                    threeJsRendererServiceAccessMock.getBabylonResourceItemMocks().forEach(babylonResourceItemMock -> {
                        if (babylonResourceItemMock.getMarkerConfig() != null) {
                            getGc().setFill(Color.YELLOW);
                            getGc().fillOval(babylonResourceItemMock.getPosition().getX() - 2 * radius,
                                    babylonResourceItemMock.getPosition().getY() - 2 * radius,
                                    4 * radius,
                                    4 * radius);
                        }
                    });
                    //--------------------------- Resource
                    getGc().setFill(Color.PINK);
                    getGc().fillOval(syncResourceItemSimpleDto.getPosition2d().getX() - radius,
                            syncResourceItemSimpleDto.getPosition2d().getY() - radius,
                            2 * radius,
                            2 * radius);

                });
                //--------------------------- Field
                ViewField viewField = getWeldBean(ResourceUiService.class).getViewField();
                if (viewField != null) {
                    strokePolygon(viewField.toList(), 1, Color.BLACK, false);
                }
            }
        });

        //---------------------------
    }
}