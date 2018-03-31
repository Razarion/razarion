package com.btxtech.shared.gameengine.datatypes.config.bot;

import java.util.List;

/**
 * Created by Beat
 * on 31.03.2018.
 */
public class BotSceneIndicationInfoContainer {
    private List<BotSceneIndicationInfo> botSceneIndicationInfos;

    public List<BotSceneIndicationInfo> getBotSceneIndicationInfos() {
        return botSceneIndicationInfos;
    }

    public BotSceneIndicationInfoContainer setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        this.botSceneIndicationInfos = botSceneIndicationInfos;
        return this;
    }
}
