import {BabylonBaseItemImpl} from './babylon-base-item.impl';

describe('Emote over a unit', () => {
  it('jumps up large for a moment and settles at its own size', () => {
    expect(BabylonBaseItemImpl.emoteScale(0)).toBeCloseTo(0.3, 5);
    expect(BabylonBaseItemImpl.emoteScale(200)).toBeCloseTo(1.7, 5);
    expect(BabylonBaseItemImpl.emoteScale(350)).toBeGreaterThan(1);
    expect(BabylonBaseItemImpl.emoteScale(350)).toBeLessThan(1.7);
    expect(BabylonBaseItemImpl.emoteScale(500)).toBeCloseTo(1, 5);
    expect(BabylonBaseItemImpl.emoteScale(3000)).toBe(1);
  });
});
