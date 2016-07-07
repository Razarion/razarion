package com.btxtech.uiservice.storyboard;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class SceneCompletionHandler implements Runnable{
    private Scene scene;

    public SceneCompletionHandler(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void run() {
        complete();
    }

    public void complete() {
        scene.onComplete(this);
    }
}
