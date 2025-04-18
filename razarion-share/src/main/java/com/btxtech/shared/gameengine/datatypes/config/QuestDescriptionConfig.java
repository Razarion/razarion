package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 24.10.2016.
 */
@JsType
public class QuestDescriptionConfig<T extends QuestDescriptionConfig<T>> implements ObjectNameIdProvider, Config {
    private Integer id;
    private String internalName;
    private String title;
    private String description;
    private int xp;
    private int razarion;
    private int crystal;
    private String passedMessage;
    private boolean hidePassedDialog;

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public T id(int id) {
        setId(id);
        return (T) this;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public T internalName(String internalName) {
        this.internalName = internalName;
        return (T) this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public T title(String title) {
        setTitle(title);
        return (T) this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T description(String description) {
        setDescription(description);
        return (T) this;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public T xp(int xp) {
        setXp(xp);
        return (T) this;
    }

    public int getRazarion() {
        return razarion;
    }

    public void setRazarion(int razarion) {
        this.razarion = razarion;
    }

    public T razarion(int razarion) {
        this.razarion = razarion;
        return (T) this;
    }

    public int getCrystal() {
        return crystal;
    }

    public void setCrystal(int crystal) {
        this.crystal = crystal;
    }

    public T crystal(int crystal) {
        this.crystal = crystal;
        return (T) this;
    }

    public String getPassedMessage() {
        return passedMessage;
    }

    public void setPassedMessage(String passedMessage) {
        this.passedMessage = passedMessage;
    }

    public T passedMessage(String passedMessage) {
        this.passedMessage = passedMessage;
        return (T) this;
    }

    public boolean isHidePassedDialog() {
        return hidePassedDialog;
    }

    public void setHidePassedDialog(boolean hidePassedDialog) {
        this.hidePassedDialog = hidePassedDialog;
    }

    public T hidePassedDialog(boolean hidePassedDialog) {
        this.hidePassedDialog = hidePassedDialog;
        return (T) this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
