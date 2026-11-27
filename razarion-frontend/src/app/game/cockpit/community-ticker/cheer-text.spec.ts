import {cheerGlyph, cheerText} from './community-ticker.component';

describe('cheerText', () => {
  const cheer = {id: 1, from: 'Jade-63', emote: 'WAVE', reply: false, bonus: 0};

  it('says who greets and how', () => {
    expect(cheerText(cheer)).toBe('Jade-63 waves at you');
    expect(cheerText({...cheer, emote: 'GG'})).toBe('Jade-63 says GG to you');
    expect(cheerText({...cheer, reply: true})).toBe('Jade-63 waved back');
  });

  it('names the bonus for a welcome that was answered', () => {
    expect(cheerText({...cheer, reply: true, bonus: 100})).toBe('Jade-63 waved back · +100 Razarion for the welcome');
  });

  it('shows the sign of the greeting', () => {
    expect(cheerGlyph('WAVE')).toBe('👋');
    expect(cheerGlyph('GG')).toBe('GG');
  });
});
