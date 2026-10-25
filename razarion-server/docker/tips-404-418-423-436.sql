-- Tips for the quests the players gave up on most above level 10 (PROD game_history since 01.09.2026, analysed 25.10.):
-- 404 pick up a box 24/35, 418 build a house 12/14, 423 kill a (Bot2) Badger 5/11, 436 kill six (Bot1) Hydras 2/4.
-- None of them had a tip. Same ids on PROD and local.
--
-- 'BOX' is a new tip kind: run this only once the client that knows it is deployed - an older client has no
-- decision for it.

-- Pick up a box with the vipers: select one, then the box; the arrow when it is out of view
UPDATE QUEST SET tipString = 'BOX', tipActorItemType_id = 3 WHERE id = 404;
-- The second unlock (level 12), the same hint as 405: the unlock button, on a phone behind the menu
UPDATE QUEST SET tipString = 'UNLOCK', tipActorItemType_id = 3 WHERE id = 417;
-- Build a house (23) with the builder
UPDATE QUEST SET tipString = 'BUILD', tipActorItemType_id = 1 WHERE id = 418;
-- Kill a (Bot2) Badger with the vipers: the Badger base lies far to the south (bot 1608, ~170/1030)
UPDATE QUEST SET tipString = 'ATTACK', tipActorItemType_id = 3 WHERE id = 423;
-- Kill six (Bot1) Hydras with the hydras of quest 435, as a group
UPDATE QUEST SET tipString = 'ATTACK', tipActorItemType_id = 12, tipGroup = 1 WHERE id = 436;
