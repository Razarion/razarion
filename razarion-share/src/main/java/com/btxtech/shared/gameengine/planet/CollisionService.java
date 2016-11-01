package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

import javax.inject.Singleton;
import java.util.Collection;

/**
 * Created by Beat
 * 16.07.2016.
 */
@Singleton
@Deprecated
public class CollisionService {
    public Vertex correctPosition(Vertex position, ItemType itemType) {
        // TODO check if in playground filed. Also check in Terrain.
        return position;
    }

    public Vertex getRallyPoint(SyncBaseItem factory, Collection<ItemType> ableToBuild) {
        throw new UnsupportedOperationException();
    }

    // TODO AttackFormationItem getDestinationHint(SyncBaseItem syncBaseItem, int range, SyncItemArea target);

    public Path setupPathToSyncMovableRandomPositionIfTaken(SyncItem syncItem) {
        throw new UnsupportedOperationException();
    }

    public Path setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination) {
        throw new UnsupportedOperationException();
    }

    // TODO Path setupPathToDestination(Index position, Index destinationHint, TerrainType terrainType, BoundingBox boundingBox);

    // TODO List<AttackFormationItem> setupDestinationHints(SyncItemArea target, List<AttackFormationItem> items);

    // TODO  List<AttackFormationItem> setupDestinationHints(SyncItem target, List<AttackFormationItem> items);

    // TODO @Deprecated
    // TODO Index getFreeRandomPosition(ItemType itemType, Rectangle region, int itemFreeRange, boolean botFree, boolean ignoreMovable);

    public boolean checkIfPathValid(Path path) {
        throw new UnsupportedOperationException();
    }
}
