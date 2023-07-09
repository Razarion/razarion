package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.renderer.ViewService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * User: beat
 * Date: 16.09.12
 * Time: 12:58
 */
@Singleton
@Deprecated
public class TerrainScrollHandler {
    public enum ScrollDirection {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        STOP
    }

    private static final int SCROLL_AUTO_MOUSE_DETECTION_WIDTH = 40;
    private static final int SCROLL_TIMER_DELAY = 30;
    private static final double SCROLL_SPEED = 60; // Meter per seconds
    // private Logger logger = Logger.getLogger(TerrainScrollHandler.class.getName());
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ViewService viewService;
    private ScrollDirection scrollDirectionXKey = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionYKey = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionXMouse = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionYMouse = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionX = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionY = ScrollDirection.STOP;
    private boolean scrollDisabled;
    private SimpleScheduledFuture simpleScheduledFuture;
    private SimpleScheduledFuture moveHandler;
    private long lastAutoScrollTimeStamp;

    @PostConstruct
    public void postConstruct() {
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(SCROLL_TIMER_DELAY, false, this::autoScroll, SimpleExecutorService.Type.SCROLL);
    }

    public void setPlanetSize(DecimalPosition size) {
    }

    public void cleanup() {
        scrollDisabled = false;
        simpleScheduledFuture.cancel();
        if (moveHandler != null) {
            moveHandler.cancel();
            moveHandler = null;
        }
    }

    public void setScrollDisabled(boolean scrollDisabled, Double bottomWidth) {
        this.scrollDisabled = scrollDisabled;
        if (scrollDisabled) {
            simpleScheduledFuture.cancel();
        }
    }

    public void executeAutoScrollKey(ScrollDirection tmpScrollDirectionX, ScrollDirection tmpScrollDirectionY) {
        if (scrollDisabled) {
            return;
        }

        if (tmpScrollDirectionX != scrollDirectionXKey || tmpScrollDirectionY != scrollDirectionYKey) {
            if (tmpScrollDirectionX != null) {
                scrollDirectionXKey = tmpScrollDirectionX;
            }
            if (tmpScrollDirectionY != null) {
                scrollDirectionYKey = tmpScrollDirectionY;
            }
            executeAutoScroll();
        }
    }

    private void executeAutoScroll() {
        ScrollDirection newScrollDirectionX = ScrollDirection.STOP;
        if (scrollDirectionXKey != ScrollDirection.STOP) {
            newScrollDirectionX = scrollDirectionXKey;
        } else if (scrollDirectionXMouse != ScrollDirection.STOP) {
            newScrollDirectionX = scrollDirectionXMouse;
        }

        ScrollDirection newScrollDirectionY = ScrollDirection.STOP;
        if (scrollDirectionYKey != ScrollDirection.STOP) {
            newScrollDirectionY = scrollDirectionYKey;
        } else if (scrollDirectionYMouse != ScrollDirection.STOP) {
            newScrollDirectionY = scrollDirectionYMouse;
        }

        if (newScrollDirectionX != scrollDirectionX || newScrollDirectionY != scrollDirectionY) {
            boolean isTimerRunningOld = scrollDirectionX != ScrollDirection.STOP || scrollDirectionY != ScrollDirection.STOP;
            boolean isTimerRunningNew = newScrollDirectionX != ScrollDirection.STOP || newScrollDirectionY != ScrollDirection.STOP;
            scrollDirectionX = newScrollDirectionX;
            scrollDirectionY = newScrollDirectionY;
            if (isTimerRunningOld != isTimerRunningNew) {
                if (isTimerRunningNew) {
                    lastAutoScrollTimeStamp = System.currentTimeMillis() - SCROLL_TIMER_DELAY;
                    autoScroll();
                    simpleScheduledFuture.start();
                } else {
                    simpleScheduledFuture.cancel();
                }
            }
        }
    }

    private void autoScroll() {
        if (scrollDirectionX == ScrollDirection.STOP && scrollDirectionY == ScrollDirection.STOP) {
            return;
        }

        double scrollX = 0;
        if (scrollDirectionX == ScrollDirection.LEFT) {
            scrollX = -1;
        } else if (scrollDirectionX == ScrollDirection.RIGHT) {
            scrollX = 1;
        }

        double scrollY = 0;
        if (scrollDirectionY == ScrollDirection.TOP) {
            scrollY = 1;
        } else if (scrollDirectionY == ScrollDirection.BOTTOM) {
            scrollY = -1;
        }

        double distance = setupScrollDistance(SCROLL_SPEED);
        DecimalPosition delta = DecimalPosition.NULL.getPointWithDistance(distance, new DecimalPosition(scrollX, scrollY), true);
        moveDelta(delta);
    }

    public void executeViewFieldConfig(ViewFieldConfig viewFieldConfig, Optional<Runnable> completionCallback) {
        if (viewFieldConfig.getSpeed() != null) {
            setScrollDisabled(true, viewFieldConfig.getBottomWidth());
            if (viewFieldConfig.getFromPosition() != null) {
                moveViewFiled(viewFieldConfig.getFromPosition());
            }
            if (viewFieldConfig.getToPosition() != null) {
                if (moveHandler != null) {
                    moveHandler.cancel();
                    moveHandler = null;
                }
                lastAutoScrollTimeStamp = System.currentTimeMillis();
                moveHandler = simpleExecutorService.scheduleAtFixedRate(SCROLL_TIMER_DELAY, true, () -> {
                    double distance = setupScrollDistance(viewFieldConfig.getSpeed());
                    DecimalPosition currentViewFiledPosition = viewService.getCurrentViewField().calculateCenter();
                    DecimalPosition newViewFiledPosition = currentViewFiledPosition.getPointWithDistance(distance, viewFieldConfig.getToPosition(), false);
                    if (currentViewFiledPosition.getDistance(newViewFiledPosition) <= distance) {
                        moveViewFiled(newViewFiledPosition);
                        finishExecuteViewPositionConfig(viewFieldConfig, completionCallback);
                    } else {
                        moveViewFiled(newViewFiledPosition);
                    }
                }, SimpleExecutorService.Type.SCROLL_AUTO);
            }
        } else {
            setScrollDisabled(viewFieldConfig.isCameraLocked(), viewFieldConfig.getBottomWidth());
            if (viewFieldConfig.getToPosition() != null) {
                moveViewFiled(viewFieldConfig.getToPosition());
            }
        }
    }

    private void finishExecuteViewPositionConfig(ViewFieldConfig viewFieldConfig, Optional<Runnable> completionCallback) {
        setScrollDisabled(viewFieldConfig.isCameraLocked(), viewFieldConfig.getBottomWidth());
        moveHandler.cancel();
        moveHandler = null;
        completionCallback.ifPresent(Runnable::run);
    }

    private void moveDelta(DecimalPosition delta) {
        moveViewFiled(viewService.getCurrentViewField().calculateCenter().add(delta));
    }

    private void moveViewFiled(DecimalPosition viewFieldPosition) {
    }

    private double setupScrollDistance(double scrollSpeed) {
        double distance = (System.currentTimeMillis() - lastAutoScrollTimeStamp) / 1000.0 * scrollSpeed;
        lastAutoScrollTimeStamp = System.currentTimeMillis();
        return distance;
    }
}
