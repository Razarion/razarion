package com.btxtech.uiservice.renderer;

public interface RenderTask<T> {
    void init(T t);

    void draw();

    void setActive(boolean active);

    void dispose();
}
