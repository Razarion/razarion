-- The player's Dockyard puts new ships 8 units east of itself instead of 4.
--
-- Dockyard and Transporter both have radius 2, so at 4 the ship spawned touching the yard - and inside the
-- yard's 3D model, which is larger than its radius. A tap on the new Transporter hit the Dockyard mesh and
-- selected the yard; the player had to arm the selection box to get the ship (phone test, 2026-10-02:
-- Dockyard at 253/75, Hydra and Transporter at 258/75).
--
-- East as before, only further: the rally point is the yard's position plus this fixed offset, recomputed
-- for every existing Dockyard when the planet starts, without a terrain check - turning it to another
-- direction could put the ships of yards already standing on other coasts on land. New yards are checked by
-- the placer ("Needs free ground to the east for the rally point").
--
-- Only the player's Dockyard, not "(Bot1) Dockyard". Item types are static config: warm-restart the planet
-- afterwards.

UPDATE BASE_ITEM_FACTORY_TYPE f
    JOIN BASE_ITEM_TYPE b ON b.factoryType_id = f.id
SET f.rallyOffsetX = 8, f.rallyOffsetY = 0
WHERE b.internalName = 'Dockyard';

SELECT b.internalName, f.rallyOffsetX, f.rallyOffsetY
FROM BASE_ITEM_FACTORY_TYPE f
         JOIN BASE_ITEM_TYPE b ON b.factoryType_id = f.id
WHERE b.internalName IN ('Dockyard', '(Bot1) Dockyard');

-- Rollback: UPDATE ... SET f.rallyOffsetX = 4, f.rallyOffsetY = 0 WHERE b.internalName = 'Dockyard';
