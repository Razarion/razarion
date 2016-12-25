package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 24.12.2016.
 */
public class AudioConfig {
    private Integer dialogOpened;
    private Integer dialogClosed;
    private Integer onQuestSActivated;
    private Integer onQuestPassed;

    public Integer getDialogOpened() {
        return dialogOpened;
    }

    public AudioConfig setDialogOpened(Integer dialogOpened) {
        this.dialogOpened = dialogOpened;
        return this;
    }

    public Integer getDialogClosed() {
        return dialogClosed;
    }

    public AudioConfig setDialogClosed(Integer dialogClosed) {
        this.dialogClosed = dialogClosed;
        return this;
    }

    public Integer getOnQuestSActivated() {
        return onQuestSActivated;
    }

    public AudioConfig setOnQuestSActivated(Integer onQuestSActivated) {
        this.onQuestSActivated = onQuestSActivated;
        return this;
    }

    public Integer getOnQuestPassed() {
        return onQuestPassed;
    }

    public AudioConfig setOnQuestPassed(Integer onQuestPassed) {
        this.onQuestPassed = onQuestPassed;
        return this;
    }
}
