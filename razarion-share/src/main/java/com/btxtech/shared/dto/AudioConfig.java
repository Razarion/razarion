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
    private Integer onOwnSingleSelection;
    private Integer onOwnMultiSelection;
    private Integer onSelectionCleared;
    private Integer onOtherSelection;
    private Integer onCommandSent;
    private Integer onBaseLost;
    private Integer terrainLoopWater;
    private Integer terrainLoopLand;

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

    public Integer getOnOwnSingleSelection() {
        return onOwnSingleSelection;
    }

    public AudioConfig setOnOwnSingleSelection(Integer onOwnSingleSelection) {
        this.onOwnSingleSelection = onOwnSingleSelection;
        return this;
    }

    public Integer getOnOwnMultiSelection() {
        return onOwnMultiSelection;
    }

    public AudioConfig setOnOwnMultiSelection(Integer onOwnMultiSelection) {
        this.onOwnMultiSelection = onOwnMultiSelection;
        return this;
    }

    public Integer getOnSelectionCleared() {
        return onSelectionCleared;
    }

    public AudioConfig setOnSelectionCleared(Integer onSelectionCleared) {
        this.onSelectionCleared = onSelectionCleared;
        return this;
    }

    public Integer getOnOtherSelection() {
        return onOtherSelection;
    }

    public AudioConfig setOnOtherSelection(Integer onOtherSelection) {
        this.onOtherSelection = onOtherSelection;
        return this;
    }

    public Integer getOnCommandSent() {
        return onCommandSent;
    }

    public AudioConfig setOnCommandSent(Integer onCommandSent) {
        this.onCommandSent = onCommandSent;
        return this;
    }

    public Integer getOnBaseLost() {
        return onBaseLost;
    }

    public AudioConfig setOnBaseLost(Integer onBaseLost) {
        this.onBaseLost = onBaseLost;
        return this;
    }

    public Integer getTerrainLoopWater() {
        return terrainLoopWater;
    }

    public AudioConfig setTerrainLoopWater(Integer terrainLoopWater) {
        this.terrainLoopWater = terrainLoopWater;
        return this;
    }

    public Integer getTerrainLoopLand() {
        return terrainLoopLand;
    }

    public AudioConfig setTerrainLoopLand(Integer terrainLoopLand) {
        this.terrainLoopLand = terrainLoopLand;
        return this;
    }
}
