package com.btxtech.client;

import com.btxtech.client.units.ItemService;
import elemental.client.Browser;
import elemental.dom.TimeoutHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 24.05.2016.
 */
@Singleton
public class ItemServiceRunner {
    @Inject
    private ItemService itemService;
    private Integer timerHandler;

    public void start() {
        if (timerHandler != null) {
            return;
        }
        timerHandler = Browser.getWindow().setInterval(new TimeoutHandler() {
            @Override
            public void onTimeoutHandler() {
                itemService.tick();
            }
        }, 100);
    }

    public void stop() {
        if (timerHandler == null) {
            return;
        }
        Browser.getWindow().clearInterval(timerHandler);
        timerHandler = null;
    }

    public boolean isRunning() {
        return timerHandler != null;
    }

    public void setRunning(boolean running) {
        if (running) {
            start();
        } else {
            stop();
        }
    }
}
