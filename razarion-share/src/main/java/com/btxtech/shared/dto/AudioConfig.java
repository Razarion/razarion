package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 24.12.2016.
 */
public class AudioConfig {
    private Integer dialogOpened;
    private Integer dialogClosed;
    private Integer onQuestActivated;
    private Integer onQuestPassed;
    private Integer onLevelUp;
    private Integer onBoxPicked;

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

    public Integer getOnQuestActivated() {
        return onQuestActivated;
    }

    public AudioConfig setOnQuestActivated(Integer onQuestActivated) {
        this.onQuestActivated = onQuestActivated;
        return this;
    }

    public Integer getOnQuestPassed() {
        return onQuestPassed;
    }

    public AudioConfig setOnQuestPassed(Integer onQuestPassed) {
        this.onQuestPassed = onQuestPassed;
        return this;
    }

    public Integer getOnLevelUp() {
        return onLevelUp;
    }

    public AudioConfig setOnLevelUp(Integer onLevelUp) {
        this.onLevelUp = onLevelUp;
        return this;
    }

    public Integer getOnBoxPicked() {
        return onBoxPicked;
    }

    public AudioConfig setOnBoxPicked(Integer onBoxPicked) {
        this.onBoxPicked = onBoxPicked;
        return this;
    }
}
