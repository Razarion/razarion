package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

/**
 * Created by Beat
 * 05.07.2016.
 */
// Better name: something with game-control, client control
public class ColdGameUiContext {
    // User
    private UserContext userContext;
    private StaticGameConfig staticGameConfig;
    private AudioConfig audioConfig;
    private GameTipVisualConfig gameTipVisualConfig;
    private InGameQuestVisualConfig inGameQuestVisualConfig;
    private WarmGameUiContext warmGameUiContext;

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public StaticGameConfig getStaticGameConfig() {
        return staticGameConfig;
    }

    public void setStaticGameConfig(StaticGameConfig staticGameConfig) {
        this.staticGameConfig = staticGameConfig;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public void setAudioConfig(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public GameTipVisualConfig getGameTipVisualConfig() {
        return gameTipVisualConfig;
    }

    public void setGameTipVisualConfig(GameTipVisualConfig gameTipVisualConfig) {
        this.gameTipVisualConfig = gameTipVisualConfig;
    }

    public InGameQuestVisualConfig getInGameQuestVisualConfig() {
        return inGameQuestVisualConfig;
    }

    public void setInGameQuestVisualConfig(InGameQuestVisualConfig inGameQuestVisualConfig) {
        this.inGameQuestVisualConfig = inGameQuestVisualConfig;
    }

    public WarmGameUiContext getWarmGameUiContext() {
        return warmGameUiContext;
    }

    public void setWarmGameUiContext(WarmGameUiContext warmGameUiContext) {
        this.warmGameUiContext = warmGameUiContext;
    }

    public ColdGameUiContext userContext(UserContext userContext) {
        setUserContext(userContext);
        return this;
    }

    public ColdGameUiContext staticGameConfig(StaticGameConfig staticGameConfig) {
        setStaticGameConfig(staticGameConfig);
        return this;
    }

    public ColdGameUiContext audioConfig(AudioConfig audioConfig) {
        setAudioConfig(audioConfig);
        return this;
    }

    public ColdGameUiContext gameTipVisualConfig(GameTipVisualConfig gameTipVisualConfig) {
        setGameTipVisualConfig(gameTipVisualConfig);
        return this;
    }

    public ColdGameUiContext inGameQuestVisualConfig(InGameQuestVisualConfig inGameQuestVisualConfig) {
        setInGameQuestVisualConfig(inGameQuestVisualConfig);
        return this;
    }

    public ColdGameUiContext warmGameUiContext(WarmGameUiContext warmGameUiContext) {
        setWarmGameUiContext(warmGameUiContext);
        return this;
    }
}
