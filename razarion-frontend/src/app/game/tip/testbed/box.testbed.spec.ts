import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Box, Unit} from './fake-world';
import {tipCase, tipGraceful} from './tip-case';

/**
 * Catalog section 5, BOX: picking up a box, quest 404 (2026-10-25). Without a tip 11 of 35 never
 * found one on PROD, and half of the boxes expired where nobody looked.
 *
 * The scene: two vipers next to the origin, a box a few units away - both on screen with the camera
 * at home - unless a case moves it.
 */
describe('Tip test bed - box', () => {

  function base(bed: TipTestbed, boxX = 8, boxY = 5): { viper: Unit, box: Box } {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    const viper = bed.own(ItemTypeId.VIPER, 0, -5);
    bed.own(ItemTypeId.VIPER, 3, -5);
    const box = bed.world.addBox(boxX, boxY);
    bed.lookAt(0, 0);
    return {viper, box};
  }

  tipCase('BOX-01 the whole of quest 404: select a viper, tap the box', bed => {
    const {viper, box} = base(bed);
    bed.activateQuest(404);
    bed.run(1500);
    bed.check(bed.onlyPrompt()?.text === 'Click to select', '"Click to select" on a viper');

    bed.click(viper);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to pick up', box.id), '"Click to pick up" on the box');

    bed.click(box);
    bed.run(500);
    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null, 'quiet while the viper drives to the box');
    bed.runUntil(() => bed.questPassed, 20000, 'quest 404 passing');
    bed.run(1000);
  });

  tipCase('BOX-02 box off screen: the arrow points at it, the minimap marks it', bed => {
    const {viper, box} = base(bed, 60, 0);
    bed.click(viper);
    bed.activateQuest(404);
    bed.run(2500);

    bed.check(bed.arrowPointsAt(box.x, box.y), 'arrow to the box');
    const marker = bed.questMarker.get();
    bed.check(marker !== null && marker.kind === 'point' && marker.x === box.x && marker.y === box.y,
      `the minimap marks the box, was ${JSON.stringify(marker)}`);

    bed.lookAt(box.x, box.y);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to pick up', box.id), '"Click to pick up" on the box once it is in view');
  });

  tipCase('BOX-03 two boxes: the prompt stands on the one nearer to the selected viper', bed => {
    const {viper} = base(bed, -10, 6);
    const near = bed.world.addBox(2, 2);
    bed.click(viper);
    bed.activateQuest(404);
    bed.run(1500);

    bed.check(bed.showsOnly('Click to pick up', near.id), '"Click to pick up" on the nearer box');
  });

  tipGraceful('BOX-04 no box on the planet: quiet, then the prompt once one drops', bed => {
    const {viper, box} = base(bed);
    bed.world.removeBox(box.id);
    bed.click(viper);
    bed.activateQuest(404);
    bed.run(1500);
    bed.check(!bed.hasGuidance(), 'nothing to show without a box');

    const dropped = bed.world.addBox(6, 4);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to pick up', dropped.id), '"Click to pick up" on the dropped box');
  });

  tipCase('BOX-05 the box is taken by someone else on the way: the next one', bed => {
    const {viper, box} = base(bed, 25, 0);
    const other = bed.world.addBox(-28, 0);
    bed.click(viper);
    bed.activateQuest(404);
    bed.run(1500);
    // The nearer one is off screen: go there and send the viper, which drives a while.
    bed.lookAt(box.x, box.y);
    bed.run(1500);
    bed.click(box);
    bed.run(1500);
    bed.world.removeBox(box.id);
    bed.run(1500);
    bed.check(bed.arrowPointsAt(other.x, other.y), 'arrow to the other box');
    bed.lookAt(other.x, other.y);
    bed.run(1500);

    bed.check(bed.showsOnly('Click to pick up', other.id), '"Click to pick up" on the other box');
  });
});
