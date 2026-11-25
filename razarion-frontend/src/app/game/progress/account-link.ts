/**
 * The sign-in link by mail to a password account lands on /game?progress=account#account=<token>
 * (ProgressController on the server). The token is the account's login token, the same one a
 * password login stores; it comes in the fragment so that no request and no log ever carries it.
 * <p>
 * Taken over here, before anything else on the page runs: the early downloads and the game's first
 * requests decide on it whether this is a registered player or a newcomer - see terrain-prefetch.
 */
const TOKEN_KEY = 'app.token';
const PREFIX = '#account=';

export function adoptAccountTokenFromLink(win: any = window): void {
  try {
    const hash: string = win.location.hash || '';
    if (!hash.startsWith(PREFIX)) {
      return;
    }
    const token = hash.substring(PREFIX.length);
    if (token) {
      win.localStorage.setItem(TOKEN_KEY, token);
    }
    win.history.replaceState(win.history.state, '', win.location.pathname + win.location.search);
  } catch (e) {
    // Storage switched off: the player signs in with his password as before.
  }
}
