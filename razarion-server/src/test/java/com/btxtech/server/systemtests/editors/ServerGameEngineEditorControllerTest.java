package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.bot.BotEnragementStateConfigEntity;
import com.btxtech.server.persistence.bot.BotItemConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.persistence.server.ServerResourceRegionConfigEntity;
import com.btxtech.server.persistence.server.StartRegionConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorController;
import com.btxtech.test.JsonAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ServerGameEngineEditorControllerTest extends AbstractCrudTest<ServerGameEngineEditorController, ServerGameEngineConfig> {
    public ServerGameEngineEditorControllerTest() {
        super(ServerGameEngineEditorController.class, ServerGameEngineConfig.class);
    }

    @Before
    public void fillTables() {
        setupPlanetDb();
        setupLevelDb();
    }

    @After
    public void cleanTables() {
        cleanTable(StartRegionConfigEntity.class);
        cleanTable(ServerResourceRegionConfigEntity.class);
        cleanTable(ServerLevelQuestEntity.class);
        cleanTable(ServerGameEngineConfigEntity.class);
        cleanTable(BotItemConfigEntity.class);
        cleanTable(BotEnragementStateConfigEntity.class);
        cleanTable(BotConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        JsonAssert.IdSuppressor[] idSuppressor = new JsonAssert.IdSuppressor[]{
                new JsonAssert.IdSuppressor("/resourceRegionConfigs", "id", true),
                new JsonAssert.IdSuppressor("/startRegionConfigs", "id", true),
                new JsonAssert.IdSuppressor("/botConfigs", "id", true)};
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.planetConfigId(PLANET_1_ID));
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.planetConfigId(PLANET_2_ID).setResourceRegionConfigs(Collections.singletonList(
                new ResourceRegionConfig().region(new PlaceConfig().position(new DecimalPosition(1, 1)).radius(9.0)))), idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.startRegionConfigs(Collections.singletonList(new StartRegionConfig().minimalLevelId(LEVEL_1_ID).internalName("xxxx").region(new PlaceConfig().polygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(1, 1),
                new DecimalPosition(2, 1),
                new DecimalPosition(2, 2))))))),
                idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getStartRegionConfigs().add(new StartRegionConfig().minimalLevelId(LEVEL_2_ID).internalName("yyy").region(new PlaceConfig().polygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(10, 10),
                new DecimalPosition(20, 10),
                new DecimalPosition(20, 20)))))),
                idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.botConfigs(Collections.singletonList(
                        new BotConfig().botEnragementStateConfigs(Collections.singletonList(
                                new BotEnragementStateConfig().botItems(Collections.singletonList(
                                        new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID))))))),
                idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getStartRegionConfigs().remove(1), idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getStartRegionConfigs().remove(0), idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getBotConfigs().remove(0), idSuppressor);
    }

    @Test
    public void updateResourceRegionConfig() {
        ServerGameEngineConfig serverGameEngineConfig = getCrudToBeTested().create();
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        getCrudToBeTested().updateResourceRegionConfig(serverGameEngineConfig.getId(), resourceRegionConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs().size(), equalTo(0));

        resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig()
                .internalName("test1")
                .region(new PlaceConfig().position(new DecimalPosition(124, 189)))
                .count(12)
                .resourceItemTypeId(RESOURCE_ITEM_TYPE_ID)
                .minDistanceToItems(43.2));


        JsonAssert.IdSuppressor[] idSuppressor = new JsonAssert.IdSuppressor[]{
                new JsonAssert.IdSuppressor("", "id", true)};

        getCrudToBeTested().updateResourceRegionConfig(serverGameEngineConfig.getId(), resourceRegionConfigs);
        JsonAssert.assertViaJson(resourceRegionConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs(), idSuppressor);

        resourceRegionConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs());
        resourceRegionConfigs.get(0).region(new PlaceConfig().position(new DecimalPosition(34, 94)))
                .count(8);
        resourceRegionConfigs.add(new ResourceRegionConfig()
                .internalName("test3")
                .region(new PlaceConfig().position(new DecimalPosition(3, 6)))
                .count(3)
                .resourceItemTypeId(RESOURCE_ITEM_TYPE_ID)
                .minDistanceToItems(43));
        getCrudToBeTested().updateResourceRegionConfig(serverGameEngineConfig.getId(), resourceRegionConfigs);
        JsonAssert.assertViaJson(resourceRegionConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs(), idSuppressor);

        resourceRegionConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs());
        resourceRegionConfigs.remove(0);
        getCrudToBeTested().updateResourceRegionConfig(serverGameEngineConfig.getId(), resourceRegionConfigs);
        JsonAssert.assertViaJson(resourceRegionConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs(), idSuppressor);

        resourceRegionConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs());
        resourceRegionConfigs.remove(0);
        getCrudToBeTested().updateResourceRegionConfig(serverGameEngineConfig.getId(), resourceRegionConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getResourceRegionConfigs().size(), equalTo(0));
    }

    @Test
    public void updateStartRegionConfig() {
        ServerGameEngineConfig serverGameEngineConfig = getCrudToBeTested().create();
        List<StartRegionConfig> startRegionConfigs = new ArrayList<>();
        getCrudToBeTested().updateStartRegionConfig(serverGameEngineConfig.getId(), startRegionConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs().size(), equalTo(0));

        startRegionConfigs = new ArrayList<>();
        startRegionConfigs.add(new StartRegionConfig()
                .internalName("StartRegionConfig 1")
                .region(new PlaceConfig().position(new DecimalPosition(180, 4444)))
                .minimalLevelId(LEVEL_1_ID)
                .noBaseViewPosition(new DecimalPosition(19, 34)));


        JsonAssert.IdSuppressor[] idSuppressor = new JsonAssert.IdSuppressor[]{
                new JsonAssert.IdSuppressor("", "id", true)};

        getCrudToBeTested().updateStartRegionConfig(serverGameEngineConfig.getId(), startRegionConfigs);
        JsonAssert.assertViaJson(startRegionConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs(), idSuppressor);

        startRegionConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs());
        startRegionConfigs.get(0).region(new PlaceConfig().position(new DecimalPosition(34, 94)))
                .minimalLevelId(LEVEL_2_ID);
        startRegionConfigs.add(new StartRegionConfig()
                .internalName("StartRegionConfig 2")
                .region(new PlaceConfig().position(new DecimalPosition(143, 4444)))
                .minimalLevelId(LEVEL_1_ID));
        getCrudToBeTested().updateStartRegionConfig(serverGameEngineConfig.getId(), startRegionConfigs);
        JsonAssert.assertViaJson(startRegionConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs(), idSuppressor);

        startRegionConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs());
        startRegionConfigs.remove(0);
        getCrudToBeTested().updateStartRegionConfig(serverGameEngineConfig.getId(), startRegionConfigs);
        JsonAssert.assertViaJson(startRegionConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs(), idSuppressor);

        startRegionConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs());
        startRegionConfigs.remove(0);
        getCrudToBeTested().updateStartRegionConfig(serverGameEngineConfig.getId(), startRegionConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs().size(), equalTo(0));
    }

    @Test
    public void updateBotConfig() {
        ServerGameEngineConfig serverGameEngineConfig = getCrudToBeTested().create();
        List<BotConfig> botConfigs = new ArrayList<>();
        getCrudToBeTested().updateBotConfig(serverGameEngineConfig.getId(), botConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs().size(), equalTo(0));

        botConfigs = new ArrayList<>();
        botConfigs.add(new BotConfig()
                .internalName("BotConfig 1")
                .botEnragementStateConfigs(Collections.singletonList(new BotEnragementStateConfig().botItems(Collections.singletonList(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID))))));


        JsonAssert.IdSuppressor[] idSuppressor = new JsonAssert.IdSuppressor[]{
                new JsonAssert.IdSuppressor("", "id", true)};

        getCrudToBeTested().updateBotConfig(serverGameEngineConfig.getId(), botConfigs);
        JsonAssert.assertViaJson(botConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs(), idSuppressor);

        botConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs());
        botConfigs.get(0).realm(new PlaceConfig().position(new DecimalPosition(34, 94)));
        botConfigs.add(new BotConfig()
                .internalName("BotConfig 1")
                .botEnragementStateConfigs(Collections.singletonList(new BotEnragementStateConfig().botItems(Collections.singletonList(new BotItemConfig().baseItemTypeId(BASE_ITEM_TYPE_TOWER_ID))))));
        getCrudToBeTested().updateBotConfig(serverGameEngineConfig.getId(), botConfigs);
        JsonAssert.assertViaJson(botConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs(), idSuppressor);

        botConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs());
        botConfigs.remove(0);
        getCrudToBeTested().updateBotConfig(serverGameEngineConfig.getId(), botConfigs);
        JsonAssert.assertViaJson(botConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs(), idSuppressor);

        botConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getBotConfigs());
        botConfigs.remove(0);
        getCrudToBeTested().updateBotConfig(serverGameEngineConfig.getId(), botConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getStartRegionConfigs().size(), equalTo(0));
    }

    @Test
    public void updateServerLevelQuestConfig() {
        ServerGameEngineConfig serverGameEngineConfig = getCrudToBeTested().create();
        List<ServerLevelQuestConfig> serverLevelQuestConfigs = new ArrayList<>();
        getCrudToBeTested().updateServerLevelQuestConfig(serverGameEngineConfig.getId(), serverLevelQuestConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs().size(), equalTo(0));

        serverLevelQuestConfigs = new ArrayList<>();
        serverLevelQuestConfigs.add(new ServerLevelQuestConfig()
                .internalName("ServerLevelQuestConfig 1")
                .minimalLevelId(LEVEL_2_ID)
                .questConfigs(Collections.singletonList(new QuestConfig()
                        .internalName("QuestConfig")
                        .conditionConfig(new ConditionConfig()
                                .comparisonConfig(new ComparisonConfig().setCount(11))
                                .conditionTrigger(ConditionTrigger.HARVEST)))));


        JsonAssert.IdSuppressor[] idSuppressor = new JsonAssert.IdSuppressor[]{
                new JsonAssert.IdSuppressor("", "id", true,
                        new JsonAssert.IdSuppressor("/questConfigs", "id", true))
        };

        getCrudToBeTested().updateServerLevelQuestConfig(serverGameEngineConfig.getId(), serverLevelQuestConfigs);
        JsonAssert.assertViaJson(serverLevelQuestConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs(), idSuppressor);

        serverLevelQuestConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs());
        serverLevelQuestConfigs.add(new ServerLevelQuestConfig()
                .internalName("ServerLevelQuestConfig 33")
                .minimalLevelId(LEVEL_3_ID)
                .questConfigs(Collections.singletonList(new QuestConfig()
                        .internalName("QuestConfig2")
                        .conditionConfig(new ConditionConfig()
                                .comparisonConfig(new ComparisonConfig().setCount(21))
                                .conditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED)))));

        getCrudToBeTested().updateServerLevelQuestConfig(serverGameEngineConfig.getId(), serverLevelQuestConfigs);
        JsonAssert.assertViaJson(serverLevelQuestConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs(), idSuppressor);

        serverLevelQuestConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs());
        serverLevelQuestConfigs.remove(0);
        getCrudToBeTested().updateServerLevelQuestConfig(serverGameEngineConfig.getId(), serverLevelQuestConfigs);
        JsonAssert.assertViaJson(serverLevelQuestConfigs, getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs(), idSuppressor);

        serverLevelQuestConfigs = new ArrayList<>(getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs());
        serverLevelQuestConfigs.remove(0);
        getCrudToBeTested().updateServerLevelQuestConfig(serverGameEngineConfig.getId(), serverLevelQuestConfigs);
        assertThat(getCrudToBeTested().read(serverGameEngineConfig.getId()).getServerLevelQuestConfigs().size(), equalTo(0));
    }
}