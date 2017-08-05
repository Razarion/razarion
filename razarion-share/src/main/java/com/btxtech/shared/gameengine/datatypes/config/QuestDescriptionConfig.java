package com.btxtech.shared.gameengine.datatypes.config;

/**
 * Created by Beat
 * 24.10.2016.
 */
public class QuestDescriptionConfig<T extends QuestDescriptionConfig> {
    private int id;
    private String internalName;
    private String title;
    private String description;
    private int xp;
    private int money;
    private int cristal;
    private String passedMessage;
    private boolean hidePassedDialog;

    public int getId() {
        return id;
    }

    public T setId(int id) {
        this.id = id;
        return (T) this;
    }

    public String getInternalName() {
        return internalName;
    }

    public T setInternalName(String internalName) {
        this.internalName = internalName;
        return (T) this;
    }

    public String getTitle() {
        return title;
    }

    public T setTitle(String title) {
        this.title = title;
        return (T) this;
    }

    public String getDescription() {
        return description;
    }

    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }

    public int getXp() {
        return xp;
    }

    public T setXp(int xp) {
        this.xp = xp;
        return (T) this;
    }

    public int getMoney() {
        return money;
    }

    public T setMoney(int money) {
        this.money = money;
        return (T) this;
    }

    public int getCristal() {
        return cristal;
    }

    public T setCristal(int cristal) {
        this.cristal = cristal;
        return (T) this;
    }

    public String getPassedMessage() {
        return passedMessage;
    }

    public T setPassedMessage(String passedMessage) {
        this.passedMessage = passedMessage;
        return (T) this;
    }

    public boolean isHidePassedDialog() {
        return hidePassedDialog;
    }

    public T setHidePassedDialog(boolean hidePassedDialog) {
        this.hidePassedDialog = hidePassedDialog;
        return (T) this;
    }
}
