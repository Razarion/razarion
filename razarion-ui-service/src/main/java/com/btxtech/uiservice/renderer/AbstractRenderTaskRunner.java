package com.btxtech.uiservice.renderer;

public abstract class AbstractRenderTaskRunner {
    private boolean enabled = true;
    private String name;

    public void prepareRender(long timeStamp) {

    }

    public abstract void draw();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
