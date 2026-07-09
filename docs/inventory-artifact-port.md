# InventoryArtifact Port — Migration & Overview

Ported the legacy `controltheland` InventoryArtifact / workshop / trader mechanics into the
new Spring Boot 4 + Angular + TeaVM stack, alongside the already-migrated `InventoryItem`.

## Feature summary

- **InventoryArtifact** — a collectible with a `Rareness` (COMMON…LEGENDARY, each with an HTML color).
- **Box drops** — a box possibility can now yield an artifact (`inventoryArtifactId`), in addition to items/crystals.
- **Workshop** — an `InventoryItem` can require a set of artifacts (`inventoryArtifactCosts`); assembling consumes them.
- **Trader** — items and artifacts with a `crystalCost` can be bought for crystals.
- **Use** — the in-game "use item" (place on map) server path is now fully wired (`ServerInventoryService.useInventoryItem`).

## Database changes (ddl-auto=update — all NEW, auto-created on local & PROD)

No manual migration required. Hibernate `ddl-auto=update` creates all of the following, because they
are new tables / new columns (it only fails to *widen* pre-existing columns — none here):

| Object | Type |
|---|---|
| `INVENTORY_ARTIFACT` | new table (rareness `VARCHAR(16)`, image FK, crystalCost) |
| `INVENTORY_ITEM_ARTIFACT_COUNT` | new child table of `INVENTORY_ITEM` (FK `inventory_item`, artifact FK, count) |
| `BOX_ITEM_TYPE_POSSIBILITY.inventory_artifact` | new FK column |
| `USER_INVENTORY_ARTIFACT` | new join table (razarion-user ↔ inventoryArtifact) |

The `rareness` column is annotated `@Enumerated(STRING) @Column(length = 16)` from the start (per the
known PROD enum-truncation rule), so no later `ALTER` is needed even when longer values are added.

After deploying, **warm-restart the planet** so the new `StaticGameConfig` (now carrying
`inventoryArtifacts`) reaches the worker/client.

## Where things live

- **Share DTOs/engine**: `Rareness`, `InventoryArtifact`, `InventoryArtifactCount`; `InventoryItem.inventoryArtifactCosts`;
  `BoxItemTypePossibility.inventoryArtifactId`; `BoxContent.inventoryArtifacts`; `StaticGameConfig`/`InventoryTypeService` artifact registry;
  `BoxService.setupBoxContent` artifact branch.
- **Server**: `InventoryArtifactEntity`/`Repository`/`Service` (registered in `ServiceProviderService`);
  `InventoryArtifactCountEntity`; `UserEntity.USER_INVENTORY_ARTIFACT` + `toInventoryInfo`;
  `UserService` buy/assemble/add primitives; `ServerInventoryService` (box loop, use, buy, assemble);
  Spring `rest/editor/InventoryItemEditorController` (closes the pre-existing gap) + `InventoryArtifactEditorController`;
  `InventoryController` public `assemble/buy` endpoints; `StaticGameConfigService` loads artifacts;
  pom typescript-generator swapped legacy JAX-RS iface → Spring controllers.
- **TeaVM bridge**: `DtoConverter.convertInventoryItem/Artifact/…`; `AngularProxyFactory` wires
  `inventoryTypeService` (getInventoryItem/Artifact + lists) and `inventoryUiService.useItemById`;
  `InventoryUiService.useItemById`.
- **Frontend**: editor `inventory-artifact-editor` + `common/inventory-artifact` selector, item editor artifact-cost UI,
  registered in `editor-dialog`; in-game `inventory` panel with Inventory / Workshop / Trader tabs.

## Not ported (deferred, by decision)

- `DbInventoryNewUser` starter-kit for new players.
- Legacy `goldLevel` (superseded by `razarion` + `crystalCost`).
