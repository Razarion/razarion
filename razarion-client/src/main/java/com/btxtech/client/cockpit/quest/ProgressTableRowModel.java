package com.btxtech.client.cockpit.quest;

/**
 * Created by Beat
 * on 09.08.2017.
 */
public class ProgressTableRowModel {
    private String statusImage;
    private String baseItemImage;
    private String text;
    private String actionWord;

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
}
