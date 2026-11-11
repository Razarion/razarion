// A crossing to the mainland that went out as a reel is announced in the planet chat, with the
// reel's link: the chat shows it behind an Instagram button (razarion-frontend chat-cockpit) and
// loads nothing from Instagram before a player taps it. It points the players at our channel
// without sending them there - the clip plays in the game, inside Instagram's own embed.
//
// Best effort, after the post: a chat that cannot be reached must never cost a post or stop the
// queue, so every failure here is a warning and nothing more. The server announces a reel once
// (ChatService.reel), so reporting one twice is harmless.

import { env } from '../../src/config.mjs';
import { get } from './instagram.mjs';
import { adminToken } from './razarion.mjs';
import { loadLedger } from './ledger.mjs';

const BASE = () => env.RAZARION_BASE_URL || 'https://www.razarion.com';

/** A moment of the left-noob-island rule (razarion-recorder rules.json): a player's first crossing. */
export function isIslandCrossing(item) {
  return item?.format === 'moment' && /^moment:left-noob-island:/.test(item.subject || '');
}

/** The ledger item of a post, or undefined. */
export function ledgerItem(entryId) {
  return (loadLedger().items || []).find((item) => item.id === entryId);
}

/**
 * Announces the post in the chat if it is an island crossing. Returns what happened, for the log:
 * 'not-a-crossing', 'announced', 'known' (the server had it), or the error message.
 */
export async function announceReelInChat(entryId, mediaId, token) {
  if (!isIslandCrossing(ledgerItem(entryId))) {
    return 'not-a-crossing';
  }
  try {
    const media = await get(`/${mediaId}`, { fields: 'permalink', access_token: token });
    if (!media?.permalink) {
      return 'no permalink from Instagram';
    }
    const res = await fetch(`${BASE()}/rest/recorder/chat/reel`, {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + (await adminToken()), 'Content-Type': 'text/plain' },
      body: media.permalink,
    });
    if (!res.ok) {
      return `chat answered HTTP ${res.status}`;
    }
    return (await res.text()).trim() === 'true' ? 'announced' : 'known';
  } catch (error) {
    return error.message;
  }
}
