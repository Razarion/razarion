package com.btxtech.gameengine.pathing;

import com.btxtech.shared.gameengine.pathing.Pathing;
import com.btxtech.shared.gameengine.pathing.Unit;

import java.util.List;

/**
 * Created by Beat
 * 16.05.2016.
 */
// TODO replace with executor service
public class BackgroundWorker {
    private Pathing pathing;
    private boolean running;
    private Runnable runnable;
    private long sleep;

    public BackgroundWorker(Pathing pathing, Runnable runnable, long sleep) {
        this.pathing = pathing;
        this.runnable = runnable;
        this.sleep = sleep;
    }

    public void start() {
        running = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (running) {
                        runnable.run();
                        Thread.sleep(sleep);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        thread.setName("Backgroundworker");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public Pathing getPathing() {
        return pathing;
    }
}
