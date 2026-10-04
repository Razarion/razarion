import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, RBL: building the base again on the Phase 2 island - quest 395 the factory,
 * 396 radar and powerplant (2026-09-25). Both count what stands in the Phase 2 start region
 * (SYNC_ITEM_POSITION with the start region as place), and level 9 allows one of each: a radar or
 * powerplant from level 3 still standing on the noob island has to be sold before the new one can
 * go up. No quest says so.
 *
 * The scene is the player after the crossing: the builder in the Phase 2 region with the camera,
 * the old base around the origin, far behind, the factory already sold.
 */
describe('Tip test bed - rebuild', () => {

  function base(bed: TipTestbed): Unit {
    bed.world.itemLimits.set(ItemTypeId.FACTORY, 1);
    bed.world.itemLimits.set(ItemTypeId.RADAR, 1);
    bed.world.itemLimits.set(ItemTypeId.POWERPLANT, 1);
    const builder = bed.own(ItemTypeId.BUILDER, 100, 60);
    bed.lookAt(100, 60);
    return builder;
  }

  function build(bed: TipTestbed, typeId: number, x: number, y: number): void {
    bed.clickBuildButton(typeId);
    bed.run(500);
    bed.place(x, y);
    bed.run(1000);
  }

  function built(bed: TipTestbed, typeId: number, builder: Unit): () => boolean {
    return () => bed.world.isIdle(builder) && [...bed.world.units.values()]
      .some(unit => unit.owner === 'own' && unit.spec.id === typeId && unit.buildup >= 1 && unit.x > 50);
  }

  tipCase('RBL-01 the whole of quest 395: select the builder, the factory button, place it', bed => {
    const builder = base(bed);
    bed.activateQuest(395);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder');

    bed.click(builder);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.FACTORY, 'hint on the factory button');

    build(bed, ItemTypeId.FACTORY, 106, 66);
    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null, 'quiet while the builder builds');
    bed.runUntil(() => bed.questPassed, 30000, 'quest 395 passing');
  });

  tipCase('RBL-02 quest 396: radar first, then the hint moves on to the powerplant', bed => {
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(396);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.RADAR, 'hint on the radar button');

    build(bed, ItemTypeId.RADAR, 106, 66);
    bed.runUntil(built(bed, ItemTypeId.RADAR, builder), 30000, 'radar built');
    bed.run(1500);
    bed.check(!bed.questPassed, 'one of two is not the quest');
    // The builder was put down when the radar was placed (2026-10-02): select it again first.
    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder');
    bed.click(builder);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.POWERPLANT, 'hint moved on to the powerplant button');

    build(bed, ItemTypeId.POWERPLANT, 94, 66);
    bed.runUntil(() => bed.questPassed, 30000, 'quest 396 passing');
  });

  tipCase('RBL-03 quest 396, the player picks the powerplant first: fine, quiet while it goes up', bed => {
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(396);
    bed.run(1500);

    build(bed, ItemTypeId.POWERPLANT, 94, 66);
    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null
      && bed.view().cockpitHintTypeId === null, 'no hint that fights the player\'s choice');
    bed.runUntil(built(bed, ItemTypeId.POWERPLANT, builder), 30000, 'powerplant built');
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), 'the builder was put down: "Click to select" first');
    bed.click(builder);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.RADAR, 'then the radar');
  });

  tipCase('RBL-04 quest 396 with radar and powerplant from level 3 on the old island: sell, then build', bed => {
    const oldRadar = bed.own(ItemTypeId.RADAR, 0, 0);
    bed.own(ItemTypeId.POWERPLANT, 10, 0);
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(396);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === null, 'no hint on a radar button the limit has greyed out');
    bed.check(bed.arrowPointsAt(0, 0), 'arrow back to the old radar');
    const marker = bed.questMarker.get();
    bed.check(marker !== null && marker.kind === 'point' && marker.x === 0 && marker.y === 0,
      `the minimap marks the old radar, was ${JSON.stringify(marker)}`);

    bed.lookAt(0, 0);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', oldRadar.id), '"Click to select" on the old radar');
    bed.click(oldRadar);
    bed.run(1500);
    bed.check(bed.view().sellHint, 'the hint on the sell button');
    bed.clickSell();
    bed.run(500);
    bed.clickSell();
    bed.run(1500);
    bed.check(!bed.view().sellHint, 'sold: the sell hint is gone');

    bed.lookAt(100, 60);
    bed.click(builder);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.RADAR, 'now the radar button');
  });

  tipCase('RBL-05 quest 396 with only the old radar left: the powerplant first, then sell the radar', bed => {
    bed.own(ItemTypeId.RADAR, 0, 0);
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(396);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.POWERPLANT, 'hint on the powerplant button, not the greyed radar');

    build(bed, ItemTypeId.POWERPLANT, 94, 66);
    bed.runUntil(built(bed, ItemTypeId.POWERPLANT, builder), 30000, 'powerplant built');
    bed.run(1500);
    bed.check(bed.arrowPointsAt(0, 0), 'then back to the old radar');
  });
});
