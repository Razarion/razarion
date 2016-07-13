package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

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

    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public void setCameraConfig(CameraConfig cameraConfig) {
        this.cameraConfig = cameraConfig;
    }

    public boolean isShowQuestSideBar() {
        return showQuestSideBar;
    }

    public void setShowQuestSideBar(boolean showQuestSideBar) {
        this.showQuestSideBar = showQuestSideBar;
    }

    public AnimatedMeshConfig getAnimatedMeshConfig() {
        return animatedMeshConfig;
    }

    public void setAnimatedMeshConfig(AnimatedMeshConfig animatedMeshConfig) {
        this.animatedMeshConfig = animatedMeshConfig;
    }
}
