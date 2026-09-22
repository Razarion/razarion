/**
 * How much picture the tip prompt takes, and how much room it therefore needs around its anchor.
 *
 * Shared by the places that have to agree on it: the prompt itself
 * ({@link BabylonItemImpl#showSelectPromptVisualization}), the watcher that hides it when it no
 * longer fits, {@link BabylonRenderServiceAccessImpl#isPromptReadable}, which the quest tips ask
 * before they decide between a prompt and the direction arrow, and the tip test bed.
 */

export const PROMPT_GEOMETRY = {
  /** The green label with the text. */
  labelHeight: 60,
  /** Gap between label and arrow in the stack panel. */
  spacing: 10,
  /** The arrow pointing from the label at the item: its length, and its width across. */
  arrowHeight: 110,
  arrowThickness: 65,
  /**
   * Clear ground between the item and the near end of the arrow. The assembly floats between the
   * two, which is the animation.
   *
   * A gap, not an offset: `linkOffset` moves the assembly's *centre*, so the distance from the
   * item depends on how long the assembly is - and beside the item it is much longer than above
   * it, because the arrow lies on its side and the label is beside it rather than over it. Taking
   * the vertical offsets over unchanged put the arrow's tail 66 px past the item and over the top
   * of it. See promptOffsets.
   */
  gapNear: 60,
  gapFar: 110
};

/**
 * How far the assembly's centre sits from the item, for an assembly of this length along the
 * direction it is offset in. Above an item that is 180 px tall this gives the 150 and 200 the
 * prompt has always used.
 */
export function promptOffsets(assemblyLengthPx: number): { near: number, far: number } {
  return {
    near: assemblyLengthPx / 2 + PROMPT_GEOMETRY.gapNear,
    far: assemblyLengthPx / 2 + PROMPT_GEOMETRY.gapFar
  };
}

/** Length of the assembly across the item when it sits beside it: arrow, gap, label. */
export function promptAssemblyLengthBeside(bubbleWidthPx: number): number {
  return PROMPT_GEOMETRY.arrowHeight + PROMPT_GEOMETRY.spacing + bubbleWidthPx;
}

/** Height of the whole assembly: label, gap, arrow. */
export const PROMPT_HEIGHT_PX =
  PROMPT_GEOMETRY.labelHeight + PROMPT_GEOMETRY.spacing + PROMPT_GEOMETRY.arrowHeight;

/**
 * Clear picture the prompt needs on one side of its anchor, at the far end of its animation and
 * before the ideal-size scaling below. A linked control is positioned by its centre, so half the
 * assembly reaches past the offset.
 */
export const PROMPT_CLEARANCE_PX = promptOffsets(PROMPT_HEIGHT_PX).far + PROMPT_HEIGHT_PX / 2;

/**
 * The picture height the numbers above were drawn for, handed to the prompt's texture as
 * `idealHeight`. Babylon then scales every pixel value - sizes, font, and the offset the label
 * floats at - by renderHeight / idealHeight, so the prompt keeps the same share of the picture on
 * every screen instead of the same pixel count.
 *
 * It used to have no ideal size at all, which meant render pixels one to one: 290 px of clearance
 * on a desktop window is a third of the picture, and on the phones that actually play the game
 * (backbuffer about 369x683 in portrait) it is 42 %. With the bottom row of the HUD taking
 * another 26 %, an item had to sit inside a band of less than a third of the height for its
 * prompt to be seen at all - and nothing checked, so the tip waited for a click on a label that
 * was cut off above the top edge. See project_quest365_out_of_view_2026_09_22.
 *
 * 1000 rather than something smaller: a desktop window is close enough to it that the prompt
 * barely changes there, while the phone drops to about two thirds. That is enough to keep
 * CLEARANCE_FRACTION under half of what is left beside the HUD, which is what makes "above or
 * below" always answerable.
 */
export const PROMPT_IDEAL_HEIGHT_PX = 1000;

/** The clearance as a share of the picture height - the same on every screen, by construction. */
export const PROMPT_CLEARANCE_FRACTION = PROMPT_CLEARANCE_PX / PROMPT_IDEAL_HEIGHT_PX;

/** Which side of the item the label sits on; the arrow points back at the item from there. */
export type PromptSide = 'above' | 'below' | 'left' | 'right';

export interface PromptFit {
  /** Whether the prompt can be seen here at all. */
  readable: boolean;
  /** Where the label goes. Meaningless when not readable. */
  side: PromptSide;
}

/** Height of the label alone - what a prompt beside the item takes vertically. */
const LABEL_HEIGHT_PX = PROMPT_GEOMETRY.labelHeight;

/**
 * Where a prompt anchored at this point in the picture can put its label, if anywhere. Screen
 * pixels from the top left, as Babylon projects them.
 *
 * Three things can hide it: the bottom row of the HUD, which the anchor may not be under, and the
 * left and right edges, which the label may not hang over - on a phone in portrait the label is a
 * good part of the width, so an item near an edge had its text cut off even when the item itself
 * sat comfortably in the middle of the picture.
 *
 * Above first, because that is what the prompt has always looked like, then below, then beside.
 * Beside needs less height and much more width, which is exactly the trade an item near the top
 * or bottom of a narrow picture wants.
 */
export function promptFitsAt(screenX: number, screenY: number, pictureWidth: number,
                             pictureHeight: number, hudBottomPixels: number,
                             assemblyWidthPx: number): PromptFit {
  const floor = pictureHeight - hudBottomPixels;
  if (screenY < 0 || screenY > floor || screenX < 0 || screenX > pictureWidth) {
    return {readable: false, side: 'above'};
  }
  const scale = pictureHeight / PROMPT_IDEAL_HEIGHT_PX;
  // Scaled like everything else the prompt is made of: assemblyWidthPx is measured against the
  // ideal height, the picture is not. Comparing the two unscaled left two bands down the sides
  // of a phone - 40 % of its width - where the guide refused a prompt it could have shown, and
  // the player saw the direction arrow until they panned a little.
  const halfWidth = assemblyWidthPx * scale / 2;
  const clearanceV = PROMPT_CLEARANCE_FRACTION * pictureHeight;
  const centred = screenX - halfWidth >= 0 && screenX + halfWidth <= pictureWidth;
  if (centred && screenY >= clearanceV) {
    return {readable: true, side: 'above'};
  }
  if (centred && screenY + clearanceV <= floor) {
    return {readable: true, side: 'below'};
  }
  // Beside: the assembly reaches its own length plus the gap to the side, and half the label's
  // height up and down.
  const beside = promptAssemblyLengthBeside(assemblyWidthPx);
  const clearanceH = (promptOffsets(beside).far + beside / 2) * scale;
  const halfLabel = LABEL_HEIGHT_PX * scale / 2;
  const verticallyFree = screenY - halfLabel >= 0 && screenY + halfLabel <= floor;
  if (verticallyFree && screenX >= clearanceH) {
    return {readable: true, side: 'left'};
  }
  if (verticallyFree && screenX + clearanceH <= pictureWidth) {
    return {readable: true, side: 'right'};
  }
  return {readable: false, side: 'above'};
}

/**
 * Roughly how wide the label plus its arrow come out, before the ideal-size scaling.
 *
 * An estimate, because the real width is only known once Babylon has laid the text out, and the
 * side has to be chosen before there is anything to lay out. It only picks a side: half a
 * character of error moves nothing the player can see.
 */
export function promptAssemblyWidthPx(text: string, withMouseIcon: boolean): number {
  const font = 18;
  return (withMouseIcon ? 60 : 0) + 20 + text.length * font * 0.55;
}

/** The longest thing a prompt ever says, for deciding before the wording is known. */
export const LONGEST_PROMPT_TEXT = 'Click to continue building';

/**
 * A device where the prompt says "Tap" instead of "Click" and drops the mouse icon, which is
 * 60 px of a picture that has none to spare.
 */
export function isTouchDevice(): boolean {
  return typeof navigator !== 'undefined' && navigator.maxTouchPoints > 0;
}
