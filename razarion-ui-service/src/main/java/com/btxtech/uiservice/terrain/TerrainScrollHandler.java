package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;

import javax.annotation.PostConstruct;
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
    private static final double DISTANCE_PER_SCROLL_TICK = SCROLL_SPEED * (double) SCROLL_TIMER_DELAY / 1000.0;
    // private Logger logger = Logger.getLogger(TerrainScrollHandler.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
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

    @PostConstruct
    public void init() {
        simpleScheduledFuture = simpleExecutorService.scheduleAtFixedRate(SCROLL_TIMER_DELAY, false, this::autoScroll, SimpleExecutorService.Type.UNSPECIFIED);
    }

    public void cleanup() {
        scrollDisabled = false;
        simpleScheduledFuture.cancel();
        if (moveHandler != null) {
            moveHandler.cancel();
            moveHandler = null;
        }
    }

    private void setScrollDisabled(boolean scrollDisabled) {
        this.scrollDisabled = scrollDisabled;
        if (scrollDisabled) {
            simpleScheduledFuture.cancel();
        } else {
            simpleScheduledFuture.start();
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
            scrollX = -DISTANCE_PER_SCROLL_TICK;
        } else if (scrollDirectionX == ScrollDirection.RIGHT) {
            scrollX = DISTANCE_PER_SCROLL_TICK;
        }

        double scrollY = 0;
        if (scrollDirectionY == ScrollDirection.TOP) {
            scrollY = DISTANCE_PER_SCROLL_TICK;
        } else if (scrollDirectionY == ScrollDirection.BOTTOM) {
            scrollY = -DISTANCE_PER_SCROLL_TICK;
        }

        // TODO check if camera is in valid playground filed. Also check in CollisionService.correctPosition()
        setCameraPosition(camera.getTranslateX() + scrollX, camera.getTranslateY() + scrollY);
    }

    public void executeCameraConfig(final CameraConfig cameraConfig, Optional<Runnable> completionCallback) {
        if (cameraConfig.getSpeed() != null) {
            setScrollDisabled(true);
            if (cameraConfig.getFromPosition() != null) {
                setCameraPosition(cameraConfig.getFromPosition().getX(), cameraConfig.getFromPosition().getY());
            }
            if (cameraConfig.getToPosition() != null) {
                if (moveHandler != null) {
                    moveHandler.cancel();
                    moveHandler = null;
                }
                moveHandler = simpleExecutorService.scheduleAtFixedRate(SCROLL_TIMER_DELAY, true, () -> {
                    DecimalPosition cameraPosition = new DecimalPosition(camera.getTranslateX(), camera.getTranslateY());
                    double distance = cameraConfig.getSpeed() * ((double) SCROLL_TIMER_DELAY / 1000.0);
                    if (cameraPosition.getDistance(cameraConfig.getToPosition()) < distance) {
                        setCameraPosition(cameraConfig.getToPosition().getX(), cameraConfig.getToPosition().getY());
                        setScrollDisabled(cameraConfig.isCameraLocked());
                        moveHandler.cancel();
                        moveHandler = null;
                        completionCallback.ifPresent(Runnable::run);
                    } else {
                        DecimalPosition newCameraPosition = cameraPosition.getPointWithDistance(distance, cameraConfig.getToPosition(), false);
                        setCameraPosition(newCameraPosition.getX(), newCameraPosition.getY());
                    }
                }, SimpleExecutorService.Type.UNSPECIFIED);
            }
        } else {
            setScrollDisabled(cameraConfig.isCameraLocked());
            if (cameraConfig.getToPosition() != null) {
                setCameraPosition(cameraConfig.getToPosition().getX(), cameraConfig.getToPosition().getY());
            }
        }
    }

    private void setCameraPosition(double xPosition, double yPosition) {
        camera.setTranslateXY(xPosition, yPosition);
        updateViewField();

        for (TerrainScrollListener terrainScrollListener : terrainScrollListeners) {
            try {
                terrainScrollListener.onScroll(currentViewField);
            } catch (Throwable t) {
                exceptionHandler.handleException("TerrainScrollHandler notify listeners", t);
            }
        }
    }

    public void updateViewField() {
        currentViewField = projectionTransformation.calculateViewField(0);
        currentAabb = currentViewField.calculateAabbRectangle();
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

    //    public static Index calculateSafeDelta(int deltaX, int deltaY, TerrainSettings terrainSettings, Rectangle viewRect) {
//        if (terrainSettings == null) {
//            return new Index(0, 0);
//        }
//        int viewOriginLeft = viewRect.startX();
//        int viewOriginTop = viewRect.startY();
//        int viewWidth = viewRect.width();
//        int viewHeight = viewRect.height();
//
//        if (viewWidth == 0 && viewHeight == 0) {
//            return new Index(0, 0);
//        }
//
//        int orgViewOriginLeft = viewOriginLeft;
//        int orgViewOriginTop = viewOriginTop;
//
//        int tmpViewOriginLeft = viewOriginLeft + deltaX;
//        int tmpViewOriginTop = viewOriginTop + deltaY;
//
//        if (tmpViewOriginLeft < 0) {
//            deltaX = deltaX - tmpViewOriginLeft;
//        } else if (tmpViewOriginLeft > terrainSettings.getPlayFieldXSize() - viewWidth - 1) {
//            deltaX = deltaX - (tmpViewOriginLeft - (terrainSettings.getPlayFieldXSize() - viewWidth)) - 1;
//        }
//        if (viewWidth >= terrainSettings.getPlayFieldXSize()) {
//            deltaX = 0;
//            viewOriginLeft = 0;
//        } else {
//            viewOriginLeft += deltaX;
//        }
//
//        if (tmpViewOriginTop < 0) {
//            deltaY = deltaY - tmpViewOriginTop;
//        } else if (tmpViewOriginTop > terrainSettings.getPlayFieldYSize() - viewHeight - 1) {
//            deltaY = deltaY - (tmpViewOriginTop - (terrainSettings.getPlayFieldYSize() - viewHeight)) - 1;
//        }
//        if (viewHeight >= terrainSettings.getPlayFieldYSize()) {
//            deltaY = 0;
//            viewOriginTop = 0;
//        } else {
//            viewOriginTop += deltaY;
//        }
//
//        if (orgViewOriginLeft == viewOriginLeft && orgViewOriginTop == viewOriginTop) {
//            // No moveDelta
//            return new Index(0, 0);
//        }
//        return new Index(deltaX, deltaY);
//    }


}
