package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
public class SceneConfig {
    private String introText;
    private boolean showQuestSideBar;
    private CameraConfig cameraConfig;
    private AnimatedMeshConfig animatedMeshConfig;
    private List<BotConfig> botConfigs;

    public String getIntroText() {
        return introText;
    }

    public SceneConfig setIntroText(String introText) {
        this.introText = introText;
        return this;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public SceneConfig setCameraConfig(CameraConfig cameraConfig) {
        this.cameraConfig = cameraConfig;
        return this;
    }

    public boolean isShowQuestSideBar() {
        return showQuestSideBar;
    }

    public SceneConfig setShowQuestSideBar(boolean showQuestSideBar) {
        this.showQuestSideBar = showQuestSideBar;
        return this;
    }

    public AnimatedMeshConfig getAnimatedMeshConfig() {
        return animatedMeshConfig;
    }

    public SceneConfig setAnimatedMeshConfig(AnimatedMeshConfig animatedMeshConfig) {
        this.animatedMeshConfig = animatedMeshConfig;
        return this;
    }

    public List<BotConfig> getBotConfigs() {
        return botConfigs;
    }

    public SceneConfig setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
        return this;
    }
}
