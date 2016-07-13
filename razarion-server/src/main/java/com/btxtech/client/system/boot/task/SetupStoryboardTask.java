package com.btxtech.client.system.boot.task;

import com.btxtech.uiservice.storyboard.StoryboardService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.05.2016.
 */
@Dependent
public class SetupStoryboardTask extends AbstractStartupTask {
    @Inject
    private StoryboardService storyboardService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        storyboardService.setup();
    }
}
