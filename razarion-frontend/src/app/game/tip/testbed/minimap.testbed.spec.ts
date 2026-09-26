import {REGION_COAST_WATER, TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, MAP: the quest marker on the minimap (2026-09-23). It is the tip's target,
 * copied: wherever the arrow or the prompt points, the map marks - with a distance the arrow does
 * not have. Scene as in the transport cases.
 */
describe('Tip test bed - minimap marker', () => {

  function scene(bed: TipTestbed) {
    const builder = bed.own(ItemTypeId.BUILDER, 0, -5);
    bed.own(ItemTypeId.DOCKYARD, 12, 0);
    const transporter = bed.own(ItemTypeId.TRANSPORTER, 12, -7);
    bed.lookAt(0, 0);
    return {builder, transporter};
  }

  tipCase('MAP-01 target off screen: the map marks the target the arrow points at', bed => {
    const {builder, transporter} = scene(bed);
    transporter.x = 50;
    transporter.y = 0;
    bed.click(builder);
    bed.activateQuest(485);
    bed.run(1500);

    bed.check(bed.arrowPointsAt(50, 0), 'arrow to the transporter');
    const marker = bed.questMarker.get();
    bed.check(marker !== null && marker.kind === 'point' && marker.x === 50 && marker.y === 0,
      `map marks the transporter, was ${JSON.stringify(marker)}`);
  });

  tipCase('MAP-05 the "go there" chip by the arrow takes the camera to the target', bed => {
    const {builder, transporter} = scene(bed);
    transporter.x = 50;
    transporter.y = 0;
    bed.click(builder);
    bed.activateQuest(485);
    bed.run(1500);
    bed.check(bed.arrowPointsAt(50, 0) && bed.view().jumpOffered, 'arrow and chip');

    bed.clickJump();
    bed.run(1500);
    bed.check(bed.view().arrowAngle === null && !bed.view().jumpOffered, 'arrow and chip gone once the target is in view');
    bed.check(bed.showsOnly('Click to load', transporter.id), '"Click to load" on the transporter');
  });

  tipCase('MAP-02 a prompt on screen: the map marks the same unit', bed => {
    const {builder} = scene(bed);
    bed.activateQuest(485);
    bed.run(1500);

    bed.check(bed.showsOnly('Click to select', builder.id), '"Click to select" on the builder');
    const marker = bed.questMarker.get();
    bed.check(marker !== null && marker.kind === 'point' && marker.x === builder.x && marker.y === builder.y,
      `map marks the builder, was ${JSON.stringify(marker)}`);
  });

  tipCase('MAP-03 the sailing quest: the water off the coast is marked', bed => {
    const {builder, transporter} = scene(bed);
    builder.containedIn = transporter.id;
    transporter.cargo.push(builder.id);
    bed.click(transporter);
    bed.lookAt(72, 32);
    bed.activateQuest(486);
    bed.run(1500);

    bed.check(bed.view().placeMarker, 'the water is marked on screen');
    const marker = bed.questMarker.get();
    bed.check(marker !== null && marker.kind === 'polygon' && marker.corners.length === REGION_COAST_WATER.length,
      `map marks the region, was ${JSON.stringify(marker)}`);
  });

  tipCase('MAP-04 quiet while working, and gone when the quest is done', bed => {
    const {builder, transporter} = scene(bed);
    bed.activateQuest(485);
    bed.run(1500);
    bed.click(builder);
    bed.run(1500);
    bed.click(transporter);
    bed.run(500);
    bed.check(bed.questMarker.get() === null, `nothing marked while the builder walks, was ${JSON.stringify(bed.questMarker.get())}`);
    bed.runUntil(() => bed.questPassed, 20000, 'quest 485 passing');
    bed.run(500);
    bed.check(bed.questMarker.get() === null, 'nothing marked after the quest');
  });
});
