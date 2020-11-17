package com.btxtech.shared.datatypes.shape.config;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;

public class Shape3DAnimationTriggerConfig {
    private String description;
    private AnimationTrigger animationTrigger;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AnimationTrigger getAnimationTrigger() {
        return animationTrigger;
    }

    public void setAnimationTrigger(AnimationTrigger animationTrigger) {
        this.animationTrigger = animationTrigger;
    }

    public Shape3DAnimationTriggerConfig description(String description) {
        setDescription(description);
        return this;
    }

    public Shape3DAnimationTriggerConfig animationTrigger(AnimationTrigger animationTrigger) {
        setAnimationTrigger(animationTrigger);
        return this;
    }
}
