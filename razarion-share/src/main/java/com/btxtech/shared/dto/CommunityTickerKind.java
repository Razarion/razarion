package com.btxtech.shared.dto;

/**
 * What another player just did, as the community ticker over the game field shows it.
 */
public enum CommunityTickerKind {
    /** A new base on the planet. */
    LANDED,
    /** A player came back to the progress kept for them. */
    RETURNED,
    /** Every level up. */
    LEVEL_UP,
    /** Unloaded the builder on the island beyond the noob island (quest 392). */
    LEFT_NOOB_ISLAND,
    /** Destroyed an item of a bot or of another player. */
    DESTROYED,
    /** Wiped out a whole bot base. */
    DEFEATED_BOT_BASE,
    /** The player's first Hydra, Transporter or Badger. */
    FIRST_UNIT,
    /** Built a unit or a building. */
    BUILT
}
