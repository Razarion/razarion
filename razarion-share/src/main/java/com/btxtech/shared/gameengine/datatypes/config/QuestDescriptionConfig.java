package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 24.10.2016.
 */
@JsType
public class QuestDescriptionConfig<T extends QuestDescriptionConfig<T>> implements ObjectNameIdProvider, Config {
    private Integer id;
    private String internalName;
    private int xp;
    private int razarion;
    private int crystal;
    private TipConfig tipConfig;

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
        setInternalName(internalName);
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
        setRazarion(razarion);
        return (T) this;
    }

    public int getCrystal() {
        return crystal;
    }

    public void setCrystal(int crystal) {
        this.crystal = crystal;
    }

    public T crystal(int crystal) {
        setCrystal(crystal);
        return (T) this;
    }

    public @Nullable TipConfig getTipConfig() {
        return tipConfig;
    }

    public void setTipConfig(@Nullable TipConfig tipConfig) {
        this.tipConfig = tipConfig;
    }

    public T tipConfig(TipConfig tipConfig) {
        setTipConfig(tipConfig);
        return (T) this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
