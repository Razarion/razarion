import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Resource, Unit} from './fake-world';
import {tipCase, tipGraceful} from './tip-case';

/**
 * Catalog section 5, MNY: too little Razarion for the button the quest needs (2026-09-25). On PROD
 * a player who lost the builder and bought a new one stood before a greyed powerplant button (362),
 * and the tip said nothing. Now it sends a harvester out and comes back to the button once the
 * money is there.
 *
 * The scene is quest 362: builder, factory, radar and harvester at home, a field on screen.
 */
describe('Tip test bed - money', () => {

  interface Scene {
    builder: Unit;
    harvester: Unit;
    field: Resource;
  }

  function base(bed: TipTestbed, withHarvester = true): Scene {
    bed.own(ItemTypeId.FACTORY, -8, 0);
    bed.own(ItemTypeId.RADAR, -8, 8);
    const builder = bed.own(ItemTypeId.BUILDER, 0, -5);
    const harvester = withHarvester ? bed.own(ItemTypeId.HARVESTER, 4, -5) : null!;
    const field = bed.world.addResource(8, 5, 200);
    bed.world.razarion = 20;
    bed.lookAt(0, 0);
    return {builder, harvester, field};
  }

  tipCase('MNY-01 too little Razarion for the powerplant: harvest, then back to the button', bed => {
    const {builder, harvester, field} = base(bed);
    bed.click(builder);
    bed.activateQuest(362);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === null, 'no hint on the greyed button');
    bed.check(bed.showsOnly('Click to select', harvester.id), '"Click to select" on the harvester');
    bed.check(bed.lastStallReason() === null || bed.stallReasons().every(reason => reason !== 'BUTTON_DISABLED'),
      'the reason is named, not just "disabled"');

    bed.click(harvester);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to harvest', field.id), '"Click to harvest" on the field');

    bed.click(field);
    bed.run(2000);
    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null, 'quiet while the money comes in');
    bed.runUntil(() => bed.world.razarion >= 35, 60000, 'enough Razarion for the powerplant');

    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', builder.id), 'back to the builder');
    bed.click(builder);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.POWERPLANT, 'hint on the powerplant button');
  });

  tipCase('MNY-02 the harvester is already at work: wait quietly for the money', bed => {
    const {builder, harvester, field} = base(bed);
    bed.click(harvester);
    bed.click(field);
    bed.run(1000);
    bed.click(builder);
    bed.activateQuest(362);
    bed.run(3000);

    bed.check(bed.view().prompts.length === 0 && bed.view().arrowAngle === null
      && bed.view().cockpitHintTypeId === null, 'nothing while the harvester works');
    bed.runUntil(() => bed.view().cockpitHintTypeId === ItemTypeId.POWERPLANT, 60000, 'the button hint once the money is there');
  });

  tipGraceful('MNY-03 no harvester at all: nothing wrong shown', bed => {
    const {builder} = base(bed, false);
    bed.click(builder);
    bed.activateQuest(362);
    bed.run(3000);

    bed.check(bed.view().cockpitHintTypeId === null && bed.view().prompts.length === 0, 'no hint on a button that cannot be used');
  });
});
