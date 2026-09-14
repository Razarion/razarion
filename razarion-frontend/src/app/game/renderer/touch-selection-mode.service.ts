import {Injectable, NgZone, inject, signal} from '@angular/core';
import {FirstInteractionTrackerService} from '../tracking/first-interaction-tracker.service';

/**
 * Whether the next finger on the field draws a selection box instead of moving the camera.
 * <p>
 * A mouse tells the two apart by itself: the left button drags a marquee, the keys and the wheel
 * move the view. A finger is the only pointer a phone has, and it was given to the camera - which
 * left a mobile player able to select exactly one unit at a time, by tapping it. An army cannot be
 * moved that way.
 * <p>
 * So the box gets a mode instead of a second button. It is armed from the icon bar, spent on the
 * next drag, and disarmed the moment that box is released: a mode the player cannot get stuck in is
 * a mode they can try without knowing what it does. Two fingers still pinch while it is armed, so
 * the camera is never fully out of reach.
 */
@Injectable({providedIn: 'root'})
export class TouchSelectionModeService {
  private readonly zone = inject(NgZone);
  private readonly firstInteractionTracker = inject(FirstInteractionTrackerService);

  /** Read by the icon bar (the armed look) and by the two pointer handlers below the renderer. */
  readonly armed = signal(false);
  /**
   * Whether a tip is currently asking the player to select a group. Set through {@link #setAsked}
   * by SelectGroupTipTask and read by the icon bar, which marks the button and puts the sentence
   * that names it above the bar.
   * <p>
   * It lives here rather than on the tip service because the game component owns the icon and
   * already has this service: reaching the tip service from there runs through ItemCockpitComponent
   * and back, and module cycles on GameComponent have broken the test run before.
   */
  readonly asked = signal(false);

  /**
   * Same reason as {@link #set} below, and the same mistake is easy to make twice: the tip task
   * writes this from its own poll timer, which is armed from the renderer and therefore runs
   * outside Angular's zone. Written straight to the signal, the tip blocks the chain correctly and
   * the line that would tell the player why is never painted.
   */
  setAsked(asked: boolean): void {
    if (this.asked() !== asked) {
      this.zone.run(() => this.asked.set(asked));
    }
  }

  toggle(): void {
    const arming = !this.armed();
    if (arming) {
      // Only the icon arms, so this is the player finding the mode. Reported here rather than on
      // the box that follows: the two are different questions, and a player who arms and then
      // taps instead of dragging is the interesting case.
      this.firstInteractionTracker.report('SELECTION_BOX_ARMED');
    }
    this.set(arming);
  }

  disarm(): void {
    if (this.armed()) {
      this.set(false);
    }
  }

  /**
   * The writes come from pointer handlers that Babylon may dispatch outside Angular's zone, and the
   * icon bar has to repaint on them - it is the only thing on screen saying the mode is on.
   */
  private set(armed: boolean): void {
    this.zone.run(() => this.armed.set(armed));
  }
}
