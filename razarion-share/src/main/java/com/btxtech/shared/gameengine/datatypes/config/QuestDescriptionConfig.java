package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

/**
 * Created by Beat
 * 24.10.2016.
 */
public class QuestDescriptionConfig<T extends QuestDescriptionConfig> implements ObjectNameIdProvider {
    private Integer id;
    private String internalName;
    private String title;
    private String description;
    private int xp;
    private int razarion;
    private int crystal;
    private String passedMessage;
    private boolean hidePassedDialog;

    public Integer getId() {
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

    public int getRazarion() {
        return razarion;
    }

    public T setRazarion(int razarion) {
        this.razarion = razarion;
        return (T) this;
    }

    public int getCrystal() {
        return crystal;
    }

    public T setCrystal(int crystal) {
        this.crystal = crystal;
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

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
