package com.btxtech.server.model.history;

/**
 * Categories of game-history events. One denormalized event log lives in the MongoDB
 * {@code game_history} collection; this enum distinguishes the rows. Ported from the old
 * controltheland {@code DbHistoryElement.Type}, reduced to the event set that has real trigger
 * points in this code base (guild/friend/alliance/finance/planet-unlock events were dropped because
 * those systems do not exist here). Bot activity is logged too, so this doubles as a debug log.
 */
public enum GameHistoryType {
    // Account / progression
    USER_LOGGED_IN,
    LEVEL_UP,
    QUEST_ACTIVATED,
    QUEST_DEACTIVATED,
    QUEST_PASSED,
    // Base lifecycle (human and bot)
    BASE_CREATED,
    BASE_DEFEATED,
    // Item lifecycle (human and bot) - only finished/created items, never build progress
    ITEM_CREATED,
    ITEM_DESTROYED,
    // Box lifecycle
    BOX_DROPPED,
    BOX_EXPIRED,
    BOX_DELETED,
    BOX_PICKED,
    CRYSTALS_FROM_BOX,
    INVENTORY_ITEM_FROM_BOX,
    INVENTORY_ARTIFACT_FROM_BOX,
    // Inventory / economy
    INVENTORY_ITEM_USED,
    INVENTORY_ITEM_BOUGHT,
    INVENTORY_ARTIFACT_BOUGHT,
    INVENTORY_ITEM_ASSEMBLED,
    // Unlock
    LEVEL_UNLOCK_USED_VIA_CRYSTALS,
    // Bot enragement (debug)
    BOT_ENRAGE_UP,
    BOT_ENRAGE_NORMAL
}
