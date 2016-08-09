package com.btxtech.server.emulation;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.PlaceConfig;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Beat
 * 09.08.2016.
 */
public class JsonPersistenceTest {

    @Test
    public void testReadJson() throws Exception {
//        JsonPersistence jsonPersistence = new JsonPersistence();
//        StoryboardConfig storyboardConfig = jsonPersistence.readJson("StoryboardConfig3.json", StoryboardConfig.class);
//        System.out.println(storyboardConfig);
    }

    @Test
    public void writeJson() throws Exception {
//        StoryboardConfig storyboardConfig = new StoryboardConfig();
//        GameEngineConfig gameEngineConfig = new GameEngineConfig();
//        PlanetConfig planetConfig = new PlanetConfig();
//        gameEngineConfig.setPlanetConfig(planetConfig);
//        storyboardConfig.setGameEngineConfig(gameEngineConfig);
//        // Setup scenes
//        List<SceneConfig> sceneConfigs = new ArrayList<>();
//        CameraConfig cameraConfig = new CameraConfig().setToPosition(new Index(200, 200));
//        List<BotConfig> botConfigs = new ArrayList<>();
//        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
//        Collection<BotItemConfig> botItems = new ArrayList<>();
//        botItems.add(new BotItemConfig().setBaseItemTypeId(ItemTypeEmulation.Id.SIMPLE_MOVABLE.ordinal()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new Index(200, 500))));
//        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
//        botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
//        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs));
//        storyboardConfig.setSceneConfigs(sceneConfigs);
//
//
//        JsonPersistence jsonPersistence = new JsonPersistence();
//        jsonPersistence.writeJson("xxx.json", storyboardConfig);
    }
}