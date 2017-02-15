package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.ViewPositionConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * User: beat
 * Date: 16.09.12
 * Time: 12:58
 */
@Singleton
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
    private ExceptionHandler exceptionHandler;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private ScrollDirection scrollDirectionXKey = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionYKey = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionXMouse = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionYMouse = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionX = ScrollDirection.STOP;
    private ScrollDirection scrollDirectionY = ScrollDirection.STOP;
    private boolean scrollDisabled;
    private SimpleScheduledFuture simpleScheduledFuture;
    private SimpleScheduledFuture moveHandler;
    private ViewField currentViewField;
    private Rectangle2D currentAabb;
    private Collection<TerrainScrollListener> terrainScrollListeners = new ArrayList<>();
    private Rectangle2D playGround;
    private long lastAutoScrollTimeStamp;

    @PostConstruct
    public void postConstruct() {
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(SCROLL_TIMER_DELAY, false, this::autoScroll, SimpleExecutorService.Type.UNSPECIFIED);
    }

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        setPlayGround(gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getPlayGround());
    }

    public void setPlayGround(Rectangle2D playGround) {
        this.playGround = playGround;
    }

    public void cleanup() {
        scrollDisabled = false;
        simpleScheduledFuture.cancel();
        if (moveHandler != null) {
            moveHandler.cancel();
            moveHandler = null;
        }
    }

    public void setScrollDisabled(boolean scrollDisabled) {
        this.scrollDisabled = scrollDisabled;
        projectionTransformation.setDisableFovYChange(scrollDisabled);
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

    public void handleMouseMoveScroll(int x, int y, int width, int height) {
        if (scrollDisabled) {
            return;
        }

        ScrollDirection tmpScrollDirectionX = ScrollDirection.STOP;
        ScrollDirection tmpScrollDirectionY = ScrollDirection.STOP;
        if (x < SCROLL_AUTO_MOUSE_DETECTION_WIDTH) {
            tmpScrollDirectionX = ScrollDirection.LEFT;
        } else if (x > width - SCROLL_AUTO_MOUSE_DETECTION_WIDTH) {
            tmpScrollDirectionX = ScrollDirection.RIGHT;
        }

        if (y < SCROLL_AUTO_MOUSE_DETECTION_WIDTH) {
            tmpScrollDirectionY = ScrollDirection.TOP;
        } else if (y > height - SCROLL_AUTO_MOUSE_DETECTION_WIDTH) {
            tmpScrollDirectionY = ScrollDirection.BOTTOM;
        }
        executeAutoScrollMouse(tmpScrollDirectionX, tmpScrollDirectionY);
    }

    public void executeAutoScrollMouse(ScrollDirection tmpScrollDirectionX, ScrollDirection tmpScrollDirectionY) {
        if (scrollDisabled) {
            return;
        }

        if (tmpScrollDirectionX != scrollDirectionXMouse || tmpScrollDirectionY != scrollDirectionYMouse) {
            scrollDirectionXMouse = tmpScrollDirectionX;
            scrollDirectionYMouse = tmpScrollDirectionY;
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
        DecimalPosition cameraPosition = DecimalPosition.NULL.getPointWithDistance(distance, new DecimalPosition(scrollX, scrollY), true).add(camera.getTranslateX(), camera.getTranslateY());
        setCameraPosition(cameraPosition);
    }

    public void executeViewPositionConfig(ViewPositionConfig viewPositionConfig, Optional<Runnable> completionCallback) {
        projectionTransformation.setDefaultFovY();
        if (viewPositionConfig.getSpeed() != null) {
            setScrollDisabled(true);
            if (viewPositionConfig.getFromPosition() != null) {
                setViewFieldCenterPosition(viewPositionConfig.getFromPosition());
            }
            if (viewPositionConfig.getToPosition() != null) {
                if (moveHandler != null) {
                    moveHandler.cancel();
                    moveHandler = null;
                }
                lastAutoScrollTimeStamp = System.currentTimeMillis();
                moveHandler = simpleExecutorService.scheduleAtFixedRate(SCROLL_TIMER_DELAY, true, () -> {
                    double distance = setupScrollDistance(viewPositionConfig.getSpeed());
                    if (currentAabb.center().getDistance(viewPositionConfig.getToPosition()) < distance) {
                        setViewFieldCenterPosition(viewPositionConfig.getToPosition());
                        finishExecuteViewPositionConfig(viewPositionConfig, completionCallback);
                    } else {
                        DecimalPosition newViewFieldCenter = currentAabb.center().getPointWithDistance(distance, viewPositionConfig.getToPosition(), false);
                        if (setViewFieldCenterPosition(newViewFieldCenter)) {
                            // Desired view fields can not be reached
                            finishExecuteViewPositionConfig(viewPositionConfig, completionCallback);
                        }
                    }
                }, SimpleExecutorService.Type.UNSPECIFIED);
            }
        } else {
            setScrollDisabled(viewPositionConfig.isCameraLocked());
            if (viewPositionConfig.getToPosition() != null) {
                setViewFieldCenterPosition(viewPositionConfig.getToPosition());
            }
        }
    }

    private void finishExecuteViewPositionConfig(ViewPositionConfig viewPositionConfig, Optional<Runnable> completionCallback) {
        setScrollDisabled(viewPositionConfig.isCameraLocked());
        moveHandler.cancel();
        moveHandler = null;
        completionCallback.ifPresent(Runnable::run);
    }

    /**
     * @param viewFieldCenterPosition desired psotion
     * @return if desired view file can not be reached due to play ground restriction
     */
    private boolean setViewFieldCenterPosition(DecimalPosition viewFieldCenterPosition) {
        if (playGround == null) {
            setCameraPosition(projectionTransformation.viewFieldCenterToCamera(viewFieldCenterPosition, 0));
            return false;
        }
        Rectangle2D viewFieldAabb;
        if (currentAabb != null) {
            viewFieldAabb = currentAabb;
        } else {
            viewFieldAabb = projectionTransformation.calculateViewField(0).calculateAabbRectangle();
        }
        Rectangle2D possibleViewFieldCenterRect = new Rectangle2D(playGround.startX() + viewFieldAabb.width() / 2.0, playGround.startY() + viewFieldAabb.height() / 2.0, playGround.width() - viewFieldAabb.width(), playGround.height() - viewFieldAabb.height());
        if (possibleViewFieldCenterRect.contains(viewFieldCenterPosition)) {
            setCameraPosition(projectionTransformation.viewFieldCenterToCamera(viewFieldCenterPosition, 0));
            return false;
        }
        DecimalPosition possibleViewFieldCenter = possibleViewFieldCenterRect.getNearestPoint(viewFieldCenterPosition);
        setCameraPosition(projectionTransformation.viewFieldCenterToCamera(possibleViewFieldCenter, 0));

        return true;
    }

    private void setCameraPosition(DecimalPosition position) {
        double correctedXPosition = position.getX();
        double correctedYPosition = position.getY();
        if (playGround != null) {
            if (currentViewField == null || currentAabb == null) {
                camera.setTranslateXY(correctedXPosition, correctedYPosition);
                update();
            }
            double deltaX = position.getX() - camera.getTranslateX();
            double deltaY = position.getY() - camera.getTranslateY();
            Rectangle2D viewFiledAabb = currentViewField.calculateAabbRectangle().translate(deltaX, deltaY);
            if (playGround.startX() > viewFiledAabb.startX()) {
                correctedXPosition += playGround.startX() - viewFiledAabb.startX();
            } else if (playGround.endX() < viewFiledAabb.endX()) {
                correctedXPosition -= viewFiledAabb.endX() - playGround.endX();
            }

            if (playGround.startY() > viewFiledAabb.startY()) {
                correctedYPosition += playGround.startY() - viewFiledAabb.startY();
            } else if (playGround.endY() < viewFiledAabb.endY()) {
                correctedYPosition -= viewFiledAabb.endY() - playGround.endY();
            }
        }
        camera.setTranslateXY(correctedXPosition, correctedYPosition);
        update();
    }

    public void updateViewField() {
        currentViewField = projectionTransformation.calculateViewField(0);
        currentAabb = currentViewField.calculateAabbRectangle();
    }

    public void update() {
        updateViewField();

        for (TerrainScrollListener terrainScrollListener : terrainScrollListeners) {
            try {
                terrainScrollListener.onScroll(currentViewField);
            } catch (Throwable t) {
                exceptionHandler.handleException("TerrainScrollHandler notify listeners", t);
            }
        }
    }

    public void addTerrainScrollListener(TerrainScrollListener terrainScrollListener) {
        terrainScrollListeners.add(terrainScrollListener);
    }

    public void removeTerrainScrollListener(TerrainScrollListener terrainScrollListener) {
        terrainScrollListeners.remove(terrainScrollListener);
    }

    public ViewField getCurrentViewField() {
        return currentViewField;
    }

    public Rectangle2D getCurrentAabb() {
        return currentAabb;
    }

    private double setupScrollDistance(double scrollSpeed) {
        double distance = (System.currentTimeMillis() - lastAutoScrollTimeStamp) / 1000.0 * scrollSpeed;
        lastAutoScrollTimeStamp = System.currentTimeMillis();
        return distance;
    }
}
