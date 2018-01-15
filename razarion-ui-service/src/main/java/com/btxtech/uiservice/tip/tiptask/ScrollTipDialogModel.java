package com.btxtech.uiservice.tip.tiptask;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 15.01.2018.
 */
public class ScrollTipDialogModel {
    private String dialogTitle;
    private String dialogMessage;
    private Integer scrollDialogMapImageId;
    private Integer scrollDialogKeyboardImageId;
    private Consumer<Runnable> dialogOpenCallback;
    private Runnable dialogCloseCallback;

    public String getDialogTitle() {
        return dialogTitle;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
    }

    public Integer getScrollDialogMapImageId() {
        return scrollDialogMapImageId;
    }

    public void setScrollDialogMapImageId(Integer scrollDialogMapImageId) {
        this.scrollDialogMapImageId = scrollDialogMapImageId;
    }

    public Integer getScrollDialogKeyboardImageId() {
        return scrollDialogKeyboardImageId;
    }

    public void setScrollDialogKeyboardImageId(Integer scrollDialogKeyboardImageId) {
        this.scrollDialogKeyboardImageId = scrollDialogKeyboardImageId;
    }

    public Consumer<Runnable> getDialogOpenCallback() {
        return dialogOpenCallback;
    }

    public void setDialogOpenCallback(Consumer<Runnable> dialogOpenCallback) {
        this.dialogOpenCallback = dialogOpenCallback;
    }

    public Runnable getDialogCloseCallback() {
        return dialogCloseCallback;
    }

    public void setDialogCloseCallback(Runnable dialogCloseCallback) {
        this.dialogCloseCallback = dialogCloseCallback;
    }
}
