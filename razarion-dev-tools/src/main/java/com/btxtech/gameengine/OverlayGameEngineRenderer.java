package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 13.10.2016.
 */
public class OverlayGameEngineRenderer extends Abstract2dRenderer {
    private static final long delay = 16;
    @Inject
    private ProjectileService projectileService;
    @Inject
    private ItemTypeService itemTypeService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Collection<BaseItemType> weaponItems;

    public void init(Canvas canvas, double scale) {
        super.init(canvas, scale);
        weaponItems = itemTypeService.getBaseItemTypes().stream().filter(baseItemType -> baseItemType.getWeaponType() != null).collect(Collectors.toList());
        startRender();
    }

    public void startRender() {
        scheduler.scheduleWithFixedDelay(() -> Platform.runLater(this::render), delay, delay, TimeUnit.MILLISECONDS);
    }

    private void render() {
        try {
            long timeStamp = System.currentTimeMillis();

            preRender();
            ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();

            for (BaseItemType weaponItem : weaponItems) {
                for (ModelMatrices modelMatrices : new ArrayList<>(projectileService.getProjectiles(weaponItem, timeStamp))) {
                    DecimalPosition position = modelMatrices.getModel().multiply(Vertex.ZERO, 1.0).toXY();
                    extendedGraphicsContext.drawPosition(position, 0.2, Color.RED);
                }
            }

            postRender();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
