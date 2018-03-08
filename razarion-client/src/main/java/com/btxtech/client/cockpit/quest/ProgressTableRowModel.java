package com.btxtech.client.cockpit.quest;


import java.util.function.Supplier;

/**
 * Created by Beat
 * on 09.08.2017.
 */
public class ProgressTableRowModel {
    private String statusImage;
    private String baseItemImage;
    private String text;
    private String actionWord;
    private Integer textRefreshInterval;
    private Supplier<String> textCallback;

    public String getStatusImage() {
        return statusImage;
    }

    public void setStatusImage(String statusImage) {
        this.statusImage = statusImage;
    }

    public String getBaseItemImage() {
        return baseItemImage;
    }

    public void setBaseItemImage(String baseItemImage) {
        this.baseItemImage = baseItemImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getActionWord() {
        return actionWord;
    }

    public void setActionWord(String actionWord) {
        this.actionWord = actionWord;
    }

    public Integer getTextRefreshInterval() {
        return textRefreshInterval;
    }

    public void setTextRefreshInterval(Integer textRefreshInterval) {
        this.textRefreshInterval = textRefreshInterval;
    }

    public Supplier<String> getTextCallback() {
        return textCallback;
    }

    public void setTextCallback(Supplier<String> textCallback) {
        this.textCallback = textCallback;
    }
}
