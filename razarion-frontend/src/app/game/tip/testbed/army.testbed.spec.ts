import {TipTestbed} from './tip-testbed';
import {ItemTypeId} from './fake-item-types';
import {Unit} from './fake-world';
import {tipCase} from './tip-case';

/**
 * Catalog section 5, ARM: the army on the Phase 2 island - quest 400, a harvester and six vipers
 * in the start region, fabricated by the factory built there in 395 (2026-09-25). Level 9 allows
 * one harvester and six vipers; the old ones on the noob island fill the limit and cannot cross,
 * so the tip sends the player back to sell them first.
 *
 * The scene: the new factory in the Phase 2 region with the camera on it, the noob island around
 * the origin far behind.
 */
describe('Tip test bed - army', () => {

  function base(bed: TipTestbed): Unit {
    bed.world.itemLimits.set(ItemTypeId.HARVESTER, 1);
    bed.world.itemLimits.set(ItemTypeId.VIPER, 6);
    bed.world.razarion = 400;
    const factory = bed.own(ItemTypeId.FACTORY, 100, 70);
    bed.lookAt(100, 60);
    return factory;
  }

  function fabricated(bed: TipTestbed, typeId: number, count: number): () => boolean {
    return () => [...bed.world.units.values()].filter(unit => unit.owner === 'own' && unit.spec.id === typeId
      && unit.x > 50).length >= count;
  }

  tipCase('ARM-01 the whole of quest 400: the harvester, then the vipers one by one', bed => {
    const factory = base(bed);
    bed.activateQuest(400);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', factory.id), '"Click to select" on the factory');

    bed.click(factory);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.HARVESTER, 'hint on the harvester button');
    bed.clickBuildButton(ItemTypeId.HARVESTER);
    bed.runUntil(fabricated(bed, ItemTypeId.HARVESTER, 1), 30000, 'harvester out');

    for (let viper = 1; viper <= 6; viper++) {
      bed.run(1500);
      bed.check(bed.view().cockpitHintTypeId === ItemTypeId.VIPER, `hint on the viper button for viper ${viper}`);
      bed.clickBuildButton(ItemTypeId.VIPER);
      bed.runUntil(fabricated(bed, ItemTypeId.VIPER, viper), 30000, `viper ${viper} out`);
    }
    bed.runUntil(() => bed.questPassed, 5000, 'quest 400 passing');
  });

  tipCase('ARM-02 the old harvester on the noob island fills the limit: sell it, then build', bed => {
    const oldHarvester = bed.own(ItemTypeId.HARVESTER, 0, 0);
    const factory = base(bed);
    bed.click(factory);
    bed.activateQuest(400);
    bed.run(1500);
    // The vipers first: the limit still allows them, the harvester has to wait for the sale.
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.VIPER, 'the viper button, not the greyed harvester');

    for (let viper = 1; viper <= 6; viper++) {
      bed.clickBuildButton(ItemTypeId.VIPER);
      bed.runUntil(fabricated(bed, ItemTypeId.VIPER, viper), 30000, `viper ${viper} out`);
      bed.run(1500);
    }
    bed.check(bed.arrowPointsAt(0, 0), 'then the arrow back to the old harvester');

    bed.lookAt(0, 0);
    bed.run(1500);
    bed.check(bed.showsOnly('Click to select', oldHarvester.id), '"Click to select" on the old harvester');
    bed.click(oldHarvester);
    bed.run(1500);
    bed.check(bed.view().sellHint, 'the hint on the sell button');
    bed.clickSell();
    bed.run(500);
    bed.clickSell();
    bed.run(1500);

    bed.lookAt(100, 60);
    bed.click(factory);
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.HARVESTER, 'now the harvester button');
    bed.clickBuildButton(ItemTypeId.HARVESTER);
    bed.runUntil(() => bed.questPassed, 30000, 'quest 400 passing');
  });

  tipCase('ARM-03 the player queues all six vipers at once: quiet while the factory works', bed => {
    const factory = base(bed);
    bed.click(factory);
    bed.activateQuest(400);
    bed.run(1500);
    for (let viper = 0; viper < 6; viper++) {
      bed.clickBuildButton(ItemTypeId.VIPER);
    }
    bed.run(3000);
    bed.check(bed.view().cockpitHintTypeId === null && bed.view().prompts.length === 0, 'nothing while the queue runs');
    bed.runUntil(fabricated(bed, ItemTypeId.VIPER, 6), 90000, 'six vipers out');
    bed.run(1500);
    bed.check(bed.view().cockpitHintTypeId === ItemTypeId.HARVESTER, 'then the harvester button');
  });
});
