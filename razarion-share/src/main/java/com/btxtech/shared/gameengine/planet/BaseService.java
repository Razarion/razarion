package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.NotYourBaseException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBaseObject;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
@Deprecated
public class BaseService {

    // ---------------------------------------------------------------------------

    public void depositResource(double price, PlayerBase playerBase) {
        throw new UnsupportedOperationException();
    }

    public void withdrawalMoney(double price, PlayerBase playerBase) throws InsufficientFundsException {
        throw new UnsupportedOperationException();
    }

    // TODO String getBaseName(PlayerBase simpleBase);

    boolean isBot(PlayerBase playerBase) {
        throw new UnsupportedOperationException();
    }

    // TODO boolean isAbandoned(PlayerBase simpleBase);

    // TODO SimpleGuild getGuild(PlayerBase simpleBase);

    // TODO Collection<BaseAttributes> getAllBaseAttributes();

    // TODO int getItemCount(PlayerBase simpleBase, int itemTypeId) throws NoSuchItemTypeException;

    public boolean isItemLimit4ItemAddingAllowed(BaseItemType newItemType, PlayerBase playerBase) throws NoSuchItemTypeException {
        throw new UnsupportedOperationException();
    }

    // TODO boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, PlayerBase simpleBase) throws NoSuchItemTypeException;

    // TODO boolean isLevelLimitation4ItemTypeExceeded(BaseItemType newItemType, int toAddCount, PlayerBase simpleBase) throws NoSuchItemTypeException;

    // TODO int getUsedHouseSpace(PlayerBase simpleBase);

    // TODO int getHouseSpace(PlayerBase simpleBase);

    // TODO boolean isHouseSpaceExceeded(PlayerBase simpleBase, BaseItemType toBeBuiltType);

    // TODO boolean isHouseSpaceExceeded(PlayerBase simpleBase, BaseItemType toBeBuiltType, int itemCountToAdd);

    public void checkBaseAccess(SyncBaseItem syncBaseItem) throws NotYourBaseException {
        throw new UnsupportedOperationException();
    }

    // TODO void sendAccountBaseUpdate(PlayerBase simpleBase);

    public void sendAccountBaseUpdate(SyncBaseObject syncBaseObject) {
        throw new UnsupportedOperationException();
    }

    // TODO void onItemCreated(SyncBaseItem syncBaseItem);

    // TODO void onItemDeleted(SyncBaseItem syncBaseItem, PlayerBase actor);

    // TODO PlayerBase getSimpleBase4Id(int baseId);

}
