/**
 * The tip of an unlock quest (405): a bubble on the button that unlocks units, which on a phone sits
 * in the menu. Quest 405 had no tip at all; 8 of 19 players never passed it and one sat 49 minutes
 * in front of it (PROD, 2026-10-21) - the blinking unlock button alone was not enough.
 * <p>
 * Plain DOM rather than a component: what it points at belongs to three different components
 * (the HUD, the phone menu, the unlock dialog), and each carries a data-tip attribute. The guide
 * evaluates every half second, which re-anchors the bubble when a panel opens or closes.
 */
export class UnlockTip {
  /** Most specific first: the dialog's Unlock button, the button that opens it, the phone menu. */
  private static readonly ANCHORS: { selector: string, text: (touch: boolean) => string }[] = [
    {selector: '[data-tip="unlock-use"]', text: touch => touch ? 'Tap to unlock' : 'Click to unlock'},
    {selector: '[data-tip="unlock-open"]', text: touch => touch ? 'Tap here to unlock units' : 'Unlock units here'},
    {selector: '[data-tip="menu"]', text: () => 'Tap here to unlock units'}
  ];
  private element: HTMLDivElement | null = null;
  private currentTarget: HTMLElement | null = null;
  /** Sideways shift of the box, kept between evaluations: measuring it includes the last one. */
  private shift = 0;
  private readonly touch = matchMedia('(pointer: coarse)').matches;

  show(): void {
    // A dialog lies over everything else: only what is in it can be pointed at. The unlock dialog
    // with nothing to unlock says itself why (the crystals); any other dialog is not the tip's.
    const dialogOpen = [...document.querySelectorAll('.p-dialog')].some(dialog => this.usable(dialog));
    for (const anchor of UnlockTip.ANCHORS) {
      const target = [...document.querySelectorAll<HTMLElement>(anchor.selector)]
        .find(candidate => this.usable(candidate) && (!dialogOpen || !!candidate.closest('.p-dialog')));
      if (target) {
        this.place(target, anchor.text(this.touch));
        return;
      }
    }
    this.hide();
  }

  hide(): void {
    this.element?.remove();
    this.element = null;
    this.currentTarget = null;
    this.shift = 0;
  }

  /** Laid out, on screen and not disabled. A closed phone panel may be hidden or moved off screen. */
  private usable(element: Element | null): element is HTMLElement {
    if (!element) {
      return false;
    }
    const rect = element.getBoundingClientRect();
    if (rect.width === 0 || rect.height === 0 || getComputedStyle(element).visibility === 'hidden'
      || rect.bottom <= 0 || rect.right <= 0 || rect.top >= window.innerHeight || rect.left >= window.innerWidth) {
      return false;
    }
    const button = element instanceof HTMLButtonElement ? element : element.querySelector('button');
    return !button?.disabled;
  }

  private place(target: HTMLElement, text: string): void {
    if (!this.element) {
      this.element = document.createElement('div');
      this.element.className = 'unlock-tip';
      this.element.innerHTML = '<div class="unlock-tip-bounce"><div class="unlock-tip-box"></div>'
        + '<img class="unlock-tip-arrow" src="babylon-gui/arrow-down.svg" alt=""></div>';
      // The bubble presses what it points at, like the build tip does.
      this.element.addEventListener('click', () => this.currentTarget?.click());
      document.body.appendChild(this.element);
    }
    this.currentTarget = target.querySelector('button') ?? target;
    const rect = target.getBoundingClientRect();
    // The HUD button is at the top edge: there the bubble hangs below it and points up.
    const below = rect.top + rect.height / 2 < window.innerHeight / 2;
    this.element.classList.toggle('unlock-tip-below', below);
    this.element.style.left = (rect.left + rect.width / 2) + 'px';
    this.element.style.top = (below ? rect.bottom : rect.top) + 'px';
    const box = this.element.querySelector('.unlock-tip-box')!;
    if (box.textContent !== text) {
      box.textContent = text;
    }
    // Kept inside the screen sideways; the arrow keeps pointing at the button.
    const boxRect = box.getBoundingClientRect();
    const shift = Math.min(0, window.innerWidth - 8 - boxRect.right + this.shift) + Math.max(0, 8 - boxRect.left + this.shift);
    if (shift !== this.shift) {
      this.shift = shift;
      (box as HTMLElement).style.transform = `translateX(${shift}px)`;
    }
  }
}
