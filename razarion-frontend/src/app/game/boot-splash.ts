/**
 * The page's loading screen, as seen from inside the application.
 *
 * index.html paints a card, a bar and three tips on the first paint, before the Angular bundle has
 * been fetched - it is the only thing that can draw at all before Angular exists. Once Angular
 * does exist it takes over with its own cover, and the page's splash has to go.
 *
 * There was more here for five days. A second loading screen drew the world being built on a
 * canvas and asked to be told the engine's real progress and the moment the terrain was on
 * screen, so that it could dissolve into the running game instead of being cut away. It was
 * measured against this one and lost by six points - see the epitaph in index.html - and the
 * three functions that served it went with it. So did a map of Noob Island below the card, which
 * drew level with the card alone (2026-09-28 to 10-14, see index.html).
 */

/**
 * Take the page's loading screen away. Called once Angular's own cover is up, so there is no
 * moment without something on screen.
 *
 * The landing-cell picker (index.html, arm land=pick) is the one part that moves over instead of
 * going: by now the choice is closed or about to be, and the picker is what tells the player where
 * he lands until the game is there. Placed before `before`, the cover's progress well, so it sits
 * where it sat on the page's card.
 */
export function removeSplash(before?: Element | null): void {
  const picker = document.getElementById('raz-land');
  if (picker && !picker.hidden && before?.parentNode) {
    before.parentNode.insertBefore(picker, before);
  }
  document.getElementById('raz-boot')?.remove();
}
