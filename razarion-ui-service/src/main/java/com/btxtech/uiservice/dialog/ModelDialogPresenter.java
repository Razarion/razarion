package com.btxtech.uiservice.dialog;

import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ModelDialogPresenter {

    void showLevelUp();

    void showQuestPassed();

    void showBaseLost();

    void showBoxPicked(BoxContent boxContent);

    void showUseInventoryItemLimitExceeded(BaseItemType baseItemType);

    void showUseInventoryHouseSpaceExceeded();

    void showLeaveStartTutorial(Runnable closeListener);

    void showMessageDialog(String title, String message);

    void showRegisterDialog();

    void showSetUserNameDialog();
}
