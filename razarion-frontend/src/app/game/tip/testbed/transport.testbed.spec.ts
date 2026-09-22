import {REGION_PHASE2, TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, TRN: crossing the water off the noob island - quest 392 split in three
 * (2026-09-22): load the builder (485), sail the loaded transporter to the coast (486), unload the
 * builder onto the Phase 2 region (392).
 *
 * The scene is the base after quest 389: the builder at the origin, the dockyard at the shore and
 * the transporter it made next to it, the camera on all three. The Phase 2 coast is 80 units to the
 * north-east, the circle of water off it at (72, 32).
 */
describe('Tip test bed - transport', () => {

  interface Scene {
    builder: Unit;
    dockyard: Unit;
    transporter: Unit;
  }

  function base(bed: TipTestbed): Scene {
    const builder = bed.own(ItemTypeId.BUILDER, 0, -5);
    const dockyard = bed.own(ItemTypeId.DOCKYARD, 12, 0);
    const transporter = bed.own(ItemTypeId.TRANSPORTER, 12, -7);
    bed.lookAt(0, 0);
    return {builder, dockyard, transporter};
  }

  /** The state quest 485 leaves behind, without playing it. */
  function loaded(scene: Scene): void {
    scene.builder.containedIn = scene.transporter.id;
    scene.transporter.cargo.push(scene.builder.id);
  }

  tipCase('TRN-01 the whole of quest 485: select the builder, tap the transporter', bed => {
    const {builder, transporter} = base(bed);
    bed.activateQuest(485);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder');

    bed.click(builder);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to load', transporter.id), '"Click to load" on the transporter');

    bed.click(transporter);
    bed.run(300);
    bed.check(bed.view().prompts.length === 0, 'quiet while the builder walks to the transporter');
    bed.runUntil(() => bed.questPassed, 20000, 'quest 485 passing');
    bed.check(builder.containedIn === transporter.id, 'the builder is inside');
  });

  tipCase('TRN-02 quest 485, the transporter off screen: the arrow points at it', bed => {
    const {builder, transporter} = base(bed);
    transporter.x = 50;
    transporter.y = 0;
    bed.click(builder);
    bed.activateQuest(485);
    bed.run(1500);

    bed.check(bed.arrowPointsAt(50, 0), 'arrow to the transporter');
    bed.check(bed.view().prompts.length === 0, 'no prompt anywhere');
    bed.lookAt(40, 0);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to load', transporter.id), '"Click to load" once it is on screen');
  });

  tipCase('TRN-03 quest 485, the transporter was sunk: build another first', bed => {
    const {dockyard, transporter} = base(bed);
    bed.world.kill(transporter.id);
    bed.activateQuest(485);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', dockyard.id), '"Click to select" on the dockyard');

    bed.click(dockyard);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.TRANSPORTER, 'hint on the transporter button');
    bed.clickBuildButton(ItemTypeId.TRANSPORTER);
    bed.runUntil(() => bed.world.ownCount(ItemTypeId.TRANSPORTER) === 1, 20000, 'transporter built');
    bed.run(1500);
    bed.check(bed.view().prompts.some(prompt => prompt.text === 'Click to select' && prompt.typeId === ItemTypeId.BUILDER),
      'back to the builder');
  });

  tipCase('TRN-04 the whole of quest 486: select the transporter, follow the arrow, tap the marked water', bed => {
    const scene = base(bed);
    loaded(scene);
    bed.activateQuest(486);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', scene.transporter.id), '"Click to select" on the transporter');

    bed.click(scene.transporter);
    bed.run(1500);
    bed.check(bed.view().arrowAngle !== null && !bed.view().placeMarker, 'arrow to the coast');

    bed.lookAt(72, 32);
    bed.run(1500);
    bed.check(bed.view().placeMarker, 'the water off the coast is marked');

    bed.clickTerrain(72, 32);
    bed.run(1000);
    bed.check(!bed.view().placeMarker && bed.view().arrowAngle === null, 'quiet while sailing');
    bed.runUntil(() => bed.questPassed, 60000, 'quest 486 passing');
  });

  tipCase('TRN-05 quest 486 with the builder not aboard: load it first', bed => {
    const {builder, transporter} = base(bed);
    bed.activateQuest(486);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder, not on the transporter');

    bed.click(builder);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to load', transporter.id), '"Click to load" on the transporter');
  });

  tipCase('TRN-06 the whole of quest 392: select the transporter, press Unload, place on the coast', bed => {
    const scene = base(bed);
    loaded(scene);
    scene.transporter.x = 72;
    scene.transporter.y = 32;
    bed.lookAt(72, 32);
    bed.activateQuest(392);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', scene.transporter.id), '"Click to select" on the transporter');

    bed.click(scene.transporter);
    bed.run(1500);
    bed.check(bed.view().unloadHint, 'hint on the Unload button');

    bed.clickUnload();
    bed.run(1500);
    bed.check(!bed.view().unloadHint && bed.view().placeMarker, 'the region is marked while placing');

    bed.place(82, 42);
    bed.runUntil(() => bed.questPassed, 5000, 'quest 392 passing');
    bed.check(scene.builder.containedIn === null, 'the builder is out');
  });

  tipCase('TRN-07 quest 392 with the transporter still at home: sail first, the Unload button later', bed => {
    const scene = base(bed);
    loaded(scene);
    bed.click(scene.transporter);
    bed.activateQuest(392);
    bed.run(1500);

    bed.check(!bed.view().unloadHint, 'no Unload hint out of reach of the coast');
    bed.check(bed.view().arrowAngle !== null, 'arrow to the coast');
    bed.lookAt(90, 50);
    bed.run(1500);
    bed.check(bed.view().placeMarker, 'the coast is marked to sail to');
    bed.clickTerrain(78, 38);
    bed.runUntil(() => bed.view().unloadHint, 60000, 'the Unload hint once the coast is in reach');
    bed.check(REGION_PHASE2.length === 4, 'region');
  });
});
