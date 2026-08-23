import {Injectable, NgZone, inject, signal} from '@angular/core';

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

  /** Read by the icon bar (the armed look) and by the two pointer handlers below the renderer. */
  readonly armed = signal(false);

  toggle(): void {
    this.set(!this.armed());
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
