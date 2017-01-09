package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.planet.PlanetService;
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
public class CockpitService {
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
        sideCockpit = sideCockpitInstance.get();
        sideCockpit.show();
    }

    public void updateLevelAndXp(UserContext userContext) {
        sideCockpit.displayXps(userContext.getXp());
        sideCockpit.displayLevel(levelService.getLevel(userContext.getLevelId()).getNumber());
    }

    public void updateResource(int resource) {
        sideCockpit.displayResources(resource);
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
