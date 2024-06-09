package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.BabylonRendererServiceAccessMock;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class InGameQuestVisualizationServiceTest extends WeldUiBaseIntegrationTest {
    @Test
    public void activateResourceInsideScrollOutside() {
        ColdGameUiContext coldGameUiContext = setup();

        getWeldBean(ResourceUiService.class).addResource(createResource(1,
                FallbackConfig.RESOURCE_ITEM_TYPE_ID,
                new DecimalPosition(150, 175)));

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
    public void activateResourceOutsideScrollInside() {
        ColdGameUiContext coldGameUiContext = setup();

        getWeldBean(ResourceUiService.class).addResource(createResource(1,
                FallbackConfig.RESOURCE_ITEM_TYPE_ID,
                new DecimalPosition(150, 250)));

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

    @Test
    public void activateBoxInsideScrollOutside() {
        ColdGameUiContext coldGameUiContext = setup();

        getWeldBean(BoxUiService.class).addBox(createBox(2,
                FallbackConfig.BOX_ITEM_TYPE_ID,
                new DecimalPosition(150, 175)));

        createBase(coldGameUiContext.getUserContext().getUserId(), 21);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.BOX_PICKED).comparisonConfig(new ComparisonConfig().count(10))));


        BabylonRendererServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(1, babylonRendererServiceAccessMock.getBabylonBoxItemMocks().size());
        BabylonRendererServiceAccessMock.BabylonBoxItemMock babylonBoxItemMock = babylonRendererServiceAccessMock.getBabylonBoxItemMocks().get(0);
        Assert.assertNotNull(babylonBoxItemMock.getMarkerConfig());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);


        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(270, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBoxItemMocks().size());
    }

    @Test
    public void activateBoxOutsideScrollInside() {
        ColdGameUiContext coldGameUiContext = setup();

        getWeldBean(BoxUiService.class).addBox(createBox(2,
                FallbackConfig.BOX_ITEM_TYPE_ID,
                new DecimalPosition(150, 250)));

        createBase(coldGameUiContext.getUserContext().getUserId(), 21);
        getWeldBean(InGameQuestVisualizationService.class).onQuestActivated(new QuestConfig().conditionConfig(
                new ConditionConfig().conditionTrigger(ConditionTrigger.BOX_PICKED).comparisonConfig(new ComparisonConfig().count(10))));

        BabylonRendererServiceAccessMock babylonRendererServiceAccessMock = getWeldBean(BabylonRendererServiceAccessMock.class);
        Assert.assertNotNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(90, Math.toDegrees(babylonRendererServiceAccessMock.getShowOutOfViewAngle()), 0.1);
        Assert.assertEquals(0, babylonRendererServiceAccessMock.getBabylonBoxItemMocks().size());

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 200,
                240, 200,
                300, 360,
                0, 360);

        Assert.assertNull(babylonRendererServiceAccessMock.getShowOutOfViewMarkerConfig());
        Assert.assertEquals(1, babylonRendererServiceAccessMock.getBabylonBoxItemMocks().size());
        BabylonRendererServiceAccessMock.BabylonBoxItemMock babylonBoxItemMock = babylonRendererServiceAccessMock.getBabylonBoxItemMocks().get(0);
        Assert.assertNotNull(babylonBoxItemMock.getMarkerConfig());
    }

    private ColdGameUiContext setup() {
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setUserContext(new UserContext()
                .userId(1)
                .unlockedItemLimit(Collections.emptyMap())
                .levelId(1));
        setupUiEnvironment(coldGameUiContext);
        setupAlarmService();

        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        getWeldBean(InputService.class).onViewFieldChanged(
                60, 40,
                240, 40,
                300, 200,
                0, 200);

        setupI18nConstants();
        setupCockpit();

        return coldGameUiContext;
    }
}