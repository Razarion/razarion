import {CommunityTickerEntry, CommunityTickerKind} from '../../../generated/razarion-share';
import {displayItemName, tickerAge, tickerText} from './community-ticker-text';

describe('Community ticker text', () => {
  function entry(kind: CommunityTickerKind, extra: Partial<CommunityTickerEntry> = {}): CommunityTickerEntry {
    return {seq: 1, timeMillis: 0, name: 'Falcon-42', kind, level: null as any, what: null as any, ...extra};
  }

  it('says what happened', () => {
    expect(tickerText(entry(CommunityTickerKind.LANDED))).toBe('Falcon-42 has landed');
    expect(tickerText(entry(CommunityTickerKind.RETURNED))).toBe('Falcon-42 is back');
    expect(tickerText(entry(CommunityTickerKind.LEVEL_UP, {level: 6}))).toBe('Falcon-42 reached level 6');
    expect(tickerText(entry(CommunityTickerKind.LEFT_NOOB_ISLAND))).toBe('Falcon-42 left the Noob Island');
    expect(tickerText(entry(CommunityTickerKind.DESTROYED, {what: '(Bot2) Spire'}))).toBe('Falcon-42 destroyed a Spire');
    expect(tickerText(entry(CommunityTickerKind.DEFEATED_BOT_BASE, {what: 'RazCore Extractor'}))).toBe('Falcon-42 wiped out RazCore Extractor');
    expect(tickerText(entry(CommunityTickerKind.FIRST_UNIT, {what: 'Hydra'}))).toBe('Falcon-42 built their first Hydra');
    expect(tickerText(entry(CommunityTickerKind.BUILT, {what: 'Factory'}))).toBe('Falcon-42 built a Factory');
  });

  it('takes "an" before a vowel', () => {
    expect(tickerText(entry(CommunityTickerKind.DESTROYED, {what: 'Extractor'}))).toBe('Falcon-42 destroyed an Extractor');
  });

  it('drops a line it cannot say', () => {
    expect(tickerText(entry(CommunityTickerKind.DESTROYED))).toBeNull();
    expect(tickerText(entry('SOMETHING_NEW' as CommunityTickerKind))).toBeNull();
  });

  it('strips the bot bracket from item names', () => {
    expect(displayItemName('(Bot1) Refinery')).toBe('Refinery');
    expect(displayItemName('Hydra')).toBe('Hydra');
  });

  it('gives no age to what just happened', () => {
    expect(tickerAge(0, 90_000)).toBeNull();
    expect(tickerAge(0, 12 * 60_000)).toBe('12 min ago');
    expect(tickerAge(0, 3 * 3_600_000)).toBe('3 h ago');
  });
});
