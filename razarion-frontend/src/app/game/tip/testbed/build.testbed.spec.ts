import {REGION_386, TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase, tipGraceful} from './tip-case';

/**
 * Catalog section 5, PLC and BLD: the build quests 358, 361, 362 and 386.
 *
 * The scene is quest 358: the start builder next to the origin, the camera on it, and the factory to
 * be placed a few units away, on screen.
 */
describe('Tip test bed - build', () => {

  function base(bed: TipTestbed): Unit {
    const builder = bed.own(ItemTypeId.BUILDER, 0, -5);
    bed.lookAt(0, 0);
    return builder;
  }

  /** The builder selected, the factory button pressed, the factory placed. */
  function placeFactory(bed: TipTestbed, builder: Unit, x: number, y: number): void {
    bed.click(builder);
    bed.run(1500);
    bed.clickBuildButton(ItemTypeId.FACTORY);
    bed.run(500);
    bed.place(x, y);
  }

  function siteOf(bed: TipTestbed, typeId: number): Unit | undefined {
    return [...bed.world.units.values()].find(unit => unit.owner === 'own' && unit.spec.id === typeId);
  }

  tipCase('SEL-01 / PLC-01 / BLD-06 / BLD-08 the whole of quest 358', bed => {
    const builder = base(bed);
    bed.activateQuest(358);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder');

    bed.click(builder);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.FACTORY, 'hint on the factory button');
    bed.check(bed.view().prompts.length === 0, 'no prompt next to the button hint');

    bed.clickBuildButton(ItemTypeId.FACTORY);
    bed.run(500);
    bed.check(bed.view().placerActive, 'placer open');
    bed.place(8, 5);
    bed.run(3000);
    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null, 'quiet while the builder drives and builds');
    bed.runUntil(() => bed.questPassed, 30000, 'quest 358 passing');
    bed.run(2000);
  });

  tipCase('PLC-06 deselected at the button hint: back to "Click to select"', bed => {
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(358);
    bed.run(1500);
    bed.deselect();
    bed.run(3000);

    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder');
    bed.check(bed.view().cockpitHintTypeId === null, 'no hint on a cockpit that is gone');
  });

  tipCase('PLC-05 selected builder drives off screen: the button hint stays', bed => {
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(358);
    bed.run(1500);
    bed.clickTerrain(0, -40);
    bed.runUntil(() => !bed.world.onScreen(builder.x, builder.y) && bed.world.isIdle(builder), 20000, 'builder off screen');
    bed.run(2000);

    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.FACTORY, 'hint on the factory button');
  });

  tipCase('BLD-04 placer cancelled: back to the button hint', bed => {
    const builder = base(bed);
    bed.activateQuest(358);
    bed.click(builder);
    bed.run(1500);
    bed.clickBuildButton(ItemTypeId.FACTORY);
    bed.run(500);
    bed.cancelPlacer();
    bed.run(2000);

    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.FACTORY, 'hint on the factory button');
  });

  tipCase('BLD-07 builder sent elsewhere before it arrives: back to the button hint', bed => {
    const builder = base(bed);
    bed.activateQuest(358);
    placeFactory(bed, builder, 20, 12);
    bed.run(1000);
    bed.clickTerrain(-8, -5);
    bed.runUntil(() => bed.world.isIdle(builder), 10000, 'builder arriving elsewhere');
    bed.run(5000);

    bed.check(siteOf(bed, ItemTypeId.FACTORY) === undefined, 'test setup: no site was laid down');
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.FACTORY, 'hint on the factory button');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): The prompt only comes once the site has not grown for ten seconds - the time a builder on its
   * way back would need. The builder is not on its way back: it stands idle elsewhere from the
   * moment it arrives, and for those seconds nothing on screen says what to do.
   */
  tipCase('BLD-09 site stalls because the builder left: "Click to continue building", then it finishes', bed => {
    const builder = base(bed);
    bed.activateQuest(358);
    placeFactory(bed, builder, 8, 5);
    bed.runUntil(() => siteOf(bed, ItemTypeId.FACTORY) !== undefined, 10000, 'site laid down');
    bed.run(1000);
    bed.clickTerrain(-10, -8);
    bed.run(13000);

    const site = siteOf(bed, ItemTypeId.FACTORY)!;
    bed.check(site.buildup < 1, 'test setup: site unfinished');
    bed.check(bed.showsOnly('Click to continue building', site.id), '"Click to continue building" on the site');
    bed.click(site);
    bed.runUntil(() => bed.questPassed, 30000, 'quest 358 passing');
  });

  /*
   * The old task chain got this wrong (removed 2026-09-18): A destroyed site looks to the build task exactly like one that scrolled out of view: its
   * instance is gone and there is no removed-listener (W5). The task keeps waiting for it to come
   * back, with TARGET_OUT_OF_VIEW, and never offers the placement again.
   */
  tipCase('BLD-11 site destroyed: back to placing', bed => {
    const builder = base(bed);
    bed.activateQuest(358);
    placeFactory(bed, builder, 8, 5);
    bed.runUntil(() => siteOf(bed, ItemTypeId.FACTORY) !== undefined, 10000, 'site laid down');
    bed.run(1000);
    bed.world.kill(siteOf(bed, ItemTypeId.FACTORY)!.id);
    bed.run(5000);

    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.FACTORY, 'hint on the factory button');
  });

  tipCase('BLD-13 finished while off screen: quest done, nothing left', bed => {
    const builder = base(bed);
    bed.activateQuest(358);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), 'test setup: "Click to select" up');
    placeFactory(bed, builder, 8, 5);
    bed.run(500);
    bed.lookAt(-60, 0);
    bed.runUntil(() => bed.questPassed, 30000, 'quest 358 passing');
    bed.run(2000);
  });

  tipCase('BLD-03 / BLD-02 quest 386: arrow into the region, place marker once it is on screen', bed => {
    const builder = base(bed);
    bed.click(builder);
    bed.activateQuest(386);
    bed.run(1500);
    bed.clickBuildButton(ItemTypeId.DOCKYARD);
    bed.run(1500);

    bed.check(bed.arrowPointsAt(60, 0), 'arrow into the region');
    bed.lookAt(60, 0);
    bed.run(1500);
    bed.check(bed.view().placeMarker, 'place marker on the region');
    bed.place(60, 0);
    bed.runUntil(() => bed.questPassed, 60000, 'quest 386 passing');
    bed.check(REGION_386.length === 4, 'region');
  });

  tipGraceful('PLC-02 too little Razarion: no hint on a disabled button', bed => {
    const builder = base(bed);
    bed.world.razarion = 20;
    bed.click(builder);
    bed.activateQuest(358);
    bed.run(3000);

    bed.check(bed.view().cockpitHintTypeId === null, 'no hint on the disabled button');
  });
});
