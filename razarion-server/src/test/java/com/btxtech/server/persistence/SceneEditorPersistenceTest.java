package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.RazAssertTestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ViewFieldConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * 16.05.2017.
 */
public class SceneEditorPersistenceTest extends ArquillianBaseTest {
    @Inject
    private SceneEditorPersistence sceneEditorPersistence;
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;

    @Test
    public void saveAllScenes() throws Exception {
        setupPlanets();

        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().setRemoveLoadingCover(true).setViewFieldConfig(new ViewFieldConfig().setFromPosition(new DecimalPosition(12.4,54.23)).setToPosition(new DecimalPosition(17.9,100)).setBottomWidth(65.23).setCameraLocked(true).setSpeed(50.34)));
        sceneEditorPersistence.saveAllScenes(GAME_UI_CONTROL_CONFIG_1_ID, sceneConfigs);

        List<SceneConfig> actualSceneConfig = gameUiControlConfigPersistence.load(Locale.ENGLISH, new UserContext().setLevelId(LEVEL_1_ID)).getWarmGameUiControlConfig().getSceneConfigs();
        Assert.assertEquals(1, actualSceneConfig.size());
        SceneConfig actual = actualSceneConfig.get(0);
        // View Config
        ViewFieldConfig viewFieldConfig = actual.getViewFieldConfig();
        RazAssertTestHelper.assertDecimalPosition(new DecimalPosition(12.4,54.23), viewFieldConfig.getFromPosition());
        RazAssertTestHelper.assertDecimalPosition(new DecimalPosition(17.9,100), viewFieldConfig.getToPosition());
        Assert.assertEquals(65.23, viewFieldConfig.getBottomWidth(), 0.0001);
        Assert.assertEquals(true, viewFieldConfig.isCameraLocked());
        Assert.assertEquals(50.34, viewFieldConfig.getSpeed(), 0.0001);
        // Div
        Assert.assertTrue(actual.isRemoveLoadingCover());

        runInTransaction(em -> {
            em.createQuery("DELETE FROM SceneEntity ").executeUpdate();
        });
        cleanPlanets();
    }

}