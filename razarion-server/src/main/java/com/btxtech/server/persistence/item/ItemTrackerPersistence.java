package com.btxtech.server.persistence.item;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.EventTranslatorTwoArg;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

/**
 * Created by Beat
 * on 01.01.2018.
 */
@Singleton
public class ItemTrackerPersistence {
    @Inject
    private ItemTrackerDisruptor itemTrackerDisruptor;
    private static final EventTranslatorTwoArg<ItemTracking, Date, PlayerBase> BASE_CREATE =
            (event, sequence,date, base) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_CREATED);
                event.setActorBaseBotId(base.getBaseId());
                event.setActorBaseBotId(base.getBotId());
                if (base.getHumanPlayerId() != null) {
                    event.setActorHumanPlayerId(base.getHumanPlayerId().getPlayerId());
                }
            };
    private static final EventTranslatorThreeArg<ItemTracking, Date, PlayerBase, PlayerBase> BASE_DELETED =
            (event, sequence, date , base, actor) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_DELETE);
                event.setTargetBaseBotId(base.getBaseId());
                event.setTargetBaseBotId(base.getBotId());
                if (base.getHumanPlayerId() != null) {
                    event.setTargetHumanPlayerId(base.getHumanPlayerId().getPlayerId());
                }
                if (actor != null) {
                    event.setActorBaseBotId(actor.getBaseId());
                    event.setActorBaseBotId(actor.getBotId());
                    if (actor.getHumanPlayerId() != null) {
                        event.setActorHumanPlayerId(actor.getHumanPlayerId().getPlayerId());
                    }
                }
            };
    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncBaseItem> BASE_ITEM_SPAWN =
            (event, sequence,date, syncBaseItem) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_ITEM_SPAWN);
                event.setItemId(syncBaseItem.getId());
                event.setDecimalPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(syncBaseItem.getBaseItemType().getId());
                event.setActorBaseBotId(syncBaseItem.getBase().getBaseId());
                event.setActorBaseBotId(syncBaseItem.getBase().getBotId());
                if (syncBaseItem.getBase().getHumanPlayerId() != null) {
                    event.setActorHumanPlayerId(syncBaseItem.getBase().getHumanPlayerId().getPlayerId());
                }
            };

    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncBaseItem> BASE_ITEM_SPAWN_DIRECTLY =
            (event, sequence,date, syncBaseItem) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_ITEM_SPAWN_DIRECTLY);
                event.setItemId(syncBaseItem.getId());
                event.setDecimalPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(syncBaseItem.getBaseItemType().getId());
                event.setActorBaseId(syncBaseItem.getBase().getBaseId());
                event.setActorBaseBotId(syncBaseItem.getBase().getBotId());
                if (syncBaseItem.getBase().getHumanPlayerId() != null) {
                    event.setActorHumanPlayerId(syncBaseItem.getBase().getHumanPlayerId().getPlayerId());
                }
            };
    private static final EventTranslatorThreeArg<ItemTracking, Date, SyncBaseItem, SyncBaseItem> BASE_ITEM_BUILT =
            (event, sequence,date, syncBaseItem, createdBy) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_ITEM_BUILT);
                event.setItemId(syncBaseItem.getId());
                event.setDecimalPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(syncBaseItem.getBaseItemType().getId());
                event.setActorBaseId(syncBaseItem.getBase().getBaseId());
                event.setActorBaseBotId(syncBaseItem.getBase().getBotId());
                if (syncBaseItem.getBase().getHumanPlayerId() != null) {
                    event.setActorHumanPlayerId(syncBaseItem.getBase().getHumanPlayerId().getPlayerId());
                }
                event.setActorItemId(createdBy.getId());
            };
    private static final EventTranslatorThreeArg<ItemTracking, Date, SyncBaseItem, SyncBaseItem> BASE_ITEM_FACTORIZED =
            (event, sequence,date, syncBaseItem, createdBy) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_ITEM_FACTORIZED);
                event.setItemId(syncBaseItem.getId());
                event.setDecimalPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(syncBaseItem.getBaseItemType().getId());
                event.setActorBaseId(syncBaseItem.getBase().getBaseId());
                event.setActorBaseBotId(syncBaseItem.getBase().getBotId());
                if (syncBaseItem.getBase().getHumanPlayerId() != null) {
                    event.setActorHumanPlayerId(syncBaseItem.getBase().getHumanPlayerId().getPlayerId());
                }
                event.setActorItemId(createdBy.getId());
            };

    private static final EventTranslatorThreeArg<ItemTracking, Date, SyncBaseItem, SyncBaseItem> BASE_ITEM_KILLED =
            (event, sequence,date, syncBaseItem, actor) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_ITEM_KILLED);
                event.setItemId(syncBaseItem.getId());
                event.setItemTypeId(syncBaseItem.getBaseItemType().getId());
                event.setDecimalPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                event.setTargetBaseId(syncBaseItem.getBase().getBaseId());
                event.setTargetBaseBotId(syncBaseItem.getBase().getBotId());
                if (syncBaseItem.getBase().getHumanPlayerId() != null) {
                    event.setTargetBaseId(syncBaseItem.getBase().getHumanPlayerId().getPlayerId());
                }
                event.setActorItemId(actor.getId());
            };
    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncBaseItem> BASE_ITEM_REMOVED =
            (event, sequence, date,syncBaseItem) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BASE_ITEM_REMOVED);
                event.setItemId(syncBaseItem.getId());
                event.setDecimalPosition(syncBaseItem.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(syncBaseItem.getBaseItemType().getId());
                event.setTargetBaseId(syncBaseItem.getBase().getBaseId());
                event.setTargetBaseBotId(syncBaseItem.getBase().getBotId());
                if (syncBaseItem.getBase().getHumanPlayerId() != null) {
                    event.setTargetBaseId(syncBaseItem.getBase().getHumanPlayerId().getPlayerId());
                }
            };
    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncResourceItem> RESOURCE_ITEM_CREATED =
            (event, sequence,date, resource) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.RESOURCE_ITEM_CREATED);
                event.setItemId(resource.getId());
                event.setDecimalPosition(resource.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(resource.getItemType().getId());
            };
    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncResourceItem> RESOURCE_ITEM_DELETED =
            (event, sequence,date, resource) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.RESOURCE_ITEM_DELETED);
                event.setItemId(resource.getId());
                event.setDecimalPosition(resource.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(resource.getItemType().getId());
            };
    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncBoxItem> BOX_ITEM_CREATED =
            (event, sequence,date, box) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BOX_ITEM_CREATED);
                event.setItemId(box.getId());
                event.setDecimalPosition(box.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(box.getItemType().getId());
            };
    private static final EventTranslatorTwoArg<ItemTracking, Date, SyncBoxItem> BOX_ITEM_DELETED =
            (event, sequence,date, box) -> {
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.BOX_ITEM_DELETED);
                event.setItemId(box.getId());
                event.setDecimalPosition(box.getSyncPhysicalArea().getPosition2d());
                event.setItemTypeId(box.getItemType().getId());
            };

    public void onBaseCreated(PlayerBaseFull playerBase) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_CREATE, new Date(), playerBase));
    }

    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actor) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_DELETED, new Date(), playerBase, actor));
    }

    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_ITEM_SPAWN, new Date(), syncBaseItem));
    }

    public void onSpawnSyncItemNoSpan(SyncBaseItem syncBaseItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_ITEM_SPAWN_DIRECTLY, new Date(), syncBaseItem));
    }

    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_ITEM_BUILT, new Date(), syncBaseItem, createdBy));
    }

    public void onFactorySyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_ITEM_FACTORIZED, new Date(), syncBaseItem, createdBy));
    }

    public void onSyncBaseItemKilled(SyncBaseItem syncBaseItem, SyncBaseItem actor) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_ITEM_KILLED, new Date(), syncBaseItem, actor));
    }

    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BASE_ITEM_REMOVED, new Date(), syncBaseItem));
    }

    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(RESOURCE_ITEM_CREATED, new Date(), syncResourceItem));
    }

    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(RESOURCE_ITEM_DELETED, new Date(), syncResourceItem));
    }

    public void onSyncBoxCreated(SyncBoxItem syncBoxItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BOX_ITEM_CREATED, new Date(), syncBoxItem));
    }

    public void onSyncBoxDeleted(SyncBoxItem syncBoxItem) {
        itemTrackerDisruptor.publishEvent(ringBuffer -> ringBuffer.publishEvent(BOX_ITEM_DELETED, new Date(), syncBoxItem));
    }
}
