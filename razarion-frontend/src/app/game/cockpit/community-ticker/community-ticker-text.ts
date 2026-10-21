import {CommunityTickerEntry, CommunityTickerKind} from '../../../generated/razarion-share';

/** "(Bot2) Spire" reads "Spire": the bracket is for the editor, not for a player. */
export function displayItemName(name: string | null | undefined): string {
  return (name ?? '').replace(/^\(Bot\d*\)\s*/, '').trim();
}

function article(word: string): string {
  return /^[aeiou]/i.test(word) ? 'an' : 'a';
}

/** One ticker line, or null for a kind this client does not know yet - a newer server may send one. */
export function tickerText(entry: CommunityTickerEntry): string | null {
  const name = entry.name;
  const what = displayItemName(entry.what);
  switch (entry.kind) {
    case CommunityTickerKind.LANDED:
      return `${name} has landed`;
    case CommunityTickerKind.RETURNED:
      return `${name} is back`;
    case CommunityTickerKind.LEVEL_UP:
      return `${name} reached level ${entry.level}`;
    case CommunityTickerKind.LEFT_NOOB_ISLAND:
      return `${name} left the Noob Island`;
    case CommunityTickerKind.DESTROYED:
      return what ? `${name} destroyed ${article(what)} ${what}` : null;
    case CommunityTickerKind.DEFEATED_BOT_BASE:
      return what ? `${name} wiped out ${what}` : null;
    case CommunityTickerKind.FIRST_UNIT:
      return what ? `${name} built their first ${what}` : null;
    case CommunityTickerKind.BUILT:
      return what ? `${name} built ${article(what)} ${what}` : null;
    default:
      return null;
  }
}

/** Nothing for the last two minutes, so a fresh event reads as now and an older one as what it is. */
export function tickerAge(timeMillis: number, now: number): string | null {
  const minutes = Math.floor((now - timeMillis) / 60000);
  if (minutes < 2) {
    return null;
  }
  if (minutes < 60) {
    return `${minutes} min ago`;
  }
  return `${Math.floor(minutes / 60)} h ago`;
}
