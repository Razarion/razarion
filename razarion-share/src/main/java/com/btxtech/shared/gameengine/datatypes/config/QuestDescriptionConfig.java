package com.btxtech.shared.gameengine.datatypes.config;

/**
 * Created by Beat
 * 24.10.2016.
 */
public class QuestDescriptionConfig<T extends QuestDescriptionConfig> {
    private String title;
    private String description;
    private int xp;
    private String passedMessage;

    public String getTitle() {
        return title;
    }

    public T setTitle(String title) {
        this.title = title;
        return (T)this;
    }

    public String getDescription() {
        return description;
    }

    public T setDescription(String description) {
        this.description = description;
        return (T)this;
    }

    public int getXp() {
        return xp;
    }

    public T setXp(int xp) {
        this.xp = xp;
        return (T)this;
    }

    public String getPassedMessage() {
        return passedMessage;
    }

    public T setPassedMessage(String passedMessage) {
        this.passedMessage = passedMessage;
        return (T)this;
    }
}
