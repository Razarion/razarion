package com.btxtech.uiservice.storyboard;

import com.btxtech.uiservice.utils.CompletionListener;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class SceneCompletionHandler implements CompletionListener{
    private Scene scene;

    public SceneCompletionHandler(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void onCompleted() {
        scene.onComplete(this);
    }
}
