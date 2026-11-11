import {chatTime, reelEmbedUrl, reelShortcode} from './chat-cockpit.component';

describe('Chat reel and time', () => {
  it('embeds only a reel or post link of the form the server keeps', () => {
    expect(reelShortcode('https://www.instagram.com/reel/Dfm54pojyCd/')).toBe('Dfm54pojyCd');
    expect(reelShortcode('https://www.instagram.com/p/Dfm54pojyCd/')).toBe('Dfm54pojyCd');
    expect(reelShortcode('https://www.instagram.com/reel/Dfm54pojyCd/?utm_source=x')).toBeNull();
    expect(reelShortcode('https://evil.example/reel/Dfm54pojyCd/')).toBeNull();
    expect(reelShortcode('javascript:alert(1)')).toBeNull();
    expect(reelShortcode(null)).toBeNull();
  });

  it('frames Instagram\'s own embed page of the reel, caption included', () => {
    expect(reelEmbedUrl('https://www.instagram.com/reel/Dfm54pojyCd/')).toBe('https://www.instagram.com/reel/Dfm54pojyCd/embed/captioned/');
    expect(reelEmbedUrl('https://evil.example/reel/Dfm54pojyCd/')).toBeNull();
  });

  it('shows the time for today and the date before that', () => {
    const now = new Date(2026, 10, 11, 20, 0);
    expect(chatTime(new Date(2026, 10, 11, 9, 5).getTime(), now)).toBe('09:05');
    expect(chatTime(new Date(2026, 10, 10, 23, 59).getTime(), now)).toBe('10.11. 23:59');
    expect(chatTime(null, now)).toBe('');
  });
});
