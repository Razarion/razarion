package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.command.LoadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.datatypes.command.UnloadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;

/**
 * Created by Beat
 * 21.04.2017.
 */
public interface ConnectionMarshaller {
    String PACKAGE_DELIMITER = "#";

    enum Package {
        // Base
        CREATE_BASE(DecimalPosition.class),
        BASE_CREATED(PlayerBaseInfo.class),
        BASE_DELETED(Integer.class),
        // Items
        SYNC_BASE_ITEM_CHANGED(SyncBaseItemInfo.class),
        SYNC_RESOURCE_ITEM_CHANGED(SyncResourceItemInfo.class),
        SYNC_ITEM_DELETED(SyncItemDeletedInfo.class),
        // Commands
        FACTORY_COMMAND(FactoryCommand.class),
        UNLOAD_CONTAINER_COMMAND(UnloadContainerCommand.class),
        ATTACK_COMMAND(AttackCommand.class),
        BUILDER_COMMAND(BuilderCommand.class),
        BUILDER_FINALIZE_COMMAND(BuilderFinalizeCommand.class),
        HARVESTER_COMMAND(HarvestCommand.class),
        LOAD_CONTAINER_COMMAND(LoadContainerCommand.class),
        MOVE_COMMAND(MoveCommand.class),
        PICK_BOX_COMMAND(PickupBoxCommand.class);

        private Class theClass;

        Package(Class theClass) {
            this.theClass = theClass;
        }

        public Class getTheClass() {
            return theClass;
        }
    }

    static String marshall(Package aPackage, String jsonParam) {
        return aPackage.name() + PACKAGE_DELIMITER + jsonParam;
    }

    static Package deMarshallPackage(String text) {
        int delimiterOffset = text.indexOf(PACKAGE_DELIMITER);
        if (delimiterOffset < 0) {
            throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + text);
        }
        return Package.valueOf(text.substring(0, delimiterOffset));
    }

    static String deMarshallPayload(String text) {
        int delimiterOffset = text.indexOf(PACKAGE_DELIMITER);
        if (delimiterOffset < 0) {
            throw new IllegalArgumentException("Can not parse msg. Delimiter missing: " + text);
        }
        return text.substring(delimiterOffset + 1);
    }
}
