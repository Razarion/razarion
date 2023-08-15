package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface QuestCockpit {
    void showQuestSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton);

    void setShowQuestInGameVisualisation();

    void onQuestProgress(QuestProgressInfo questProgressInfo);

    void setBotSceneIndicationInfos();
}
