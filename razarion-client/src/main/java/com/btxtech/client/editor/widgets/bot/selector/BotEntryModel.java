package com.btxtech.client.editor.widgets.bot.selector;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 09.12.2017.
 */
public class BotEntryModel {
    private Integer botId;
    private Runnable updateCallback;
    private Consumer<BotEntryModel> removeCallback;

    public BotEntryModel(Integer botId, Runnable updateCallback, Consumer<BotEntryModel> removeCallback) {
        this.botId = botId;
        this.updateCallback = updateCallback;
        this.removeCallback = removeCallback;
    }

    public void remove() {
        removeCallback.accept(this);
    }

    public void change(int botId) {
        this.botId = botId;
        updateCallback.run();
    }

    public Integer getBotId() {
        return botId;
    }
}
