package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.uiservice.control.GameUiControl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Function;

/**
 * Created by Beat
 * 16.11.2016.
 */
@ApplicationScoped
public class CockpitService implements PlanetTickListener {
    @Inject
    private PlanetService planetService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private LevelService levelService;
    @Inject
    private Instance<SideCockpit> sideCockpitInstance;
    private SideCockpit sideCockpit;
    private Function<Integer, Rectangle> inventoryPositionProvider;

    @PostConstruct
    public void postConstruct() {
        planetService.addTickListener(this); // TODO does not work. This code is in the worker now
        sideCockpit = sideCockpitInstance.get();
        sideCockpit.show();
    }

    public void init() {
        // TODO set planet info
    }

    @Override
    public void onPostTick() {
        sideCockpit.displayResources(gameUiControl.getResources());
        UserContext userContext = gameUiControl.getUserContext();
        sideCockpit.displayXps(userContext.getXp());
        sideCockpit.displayLevel(levelService.getLevel(userContext.getLevelId()).getNumber());
    }

    public Rectangle getInventoryButtonLocation() {
        return sideCockpit.getInventoryDialogButtonLocation();
    }

    public Rectangle getInventoryUseButtonLocation(int inventoryItemId) {
        if (inventoryPositionProvider == null) {
            throw new IllegalStateException("InventoryDialog is no shown");
        }
        return inventoryPositionProvider.apply(inventoryItemId);
    }

    public void onInventoryDialogOpened(Function<Integer, Rectangle> inventoryPositionProvider) {
        this.inventoryPositionProvider = inventoryPositionProvider;
    }

    public void onInventoryDialogClosed() {
        inventoryPositionProvider = null;
    }
}
