import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {DecimalPosition, Diplomacy, MarkerConfig, PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {ViewField, ViewFieldListener} from '../../renderer/view-field';
import {TipStallReason, TipTaskName} from '../tip-stall';

interface Rectangle {
  x: number;
  y: number;
  width: number;
  height: number;
}

export class SendBuildCommandTipTask extends AbstractTipTask implements ViewFieldListener {
  /** Buildup has no listener slot of its own to spare - the item cockpit owns it. So: poll. */
  private static readonly BUILDUP_POLL_MILLIS = 1000;
  /**
   * Buildup standing still this long means nobody is working on the site - the classic case is
   * the builder driving off mid construction. Worth its own name in the tracking data: the tip
   * is up and correct, the player just does not know they have to send the builder back.
   */
  private static readonly NO_PROGRESS_MILLIS = 10000;
  /**
   * How long the builder is given to take up the order before its idle state is believed. Right
   * after the placement it can report idle for a tick or two, before the engine has handed it
   * the command.
   */
  private static readonly BUILD_ORDER_SETTLE_MILLIS = 3000;
  private toBeFinalized: BabylonBaseItemImpl | null = null;
  private toBeFinalizedId: number | null = null;
  private buildupPollTimeout: ReturnType<typeof setTimeout> | null = null;
  private lastBuildup: number | null = null;
  private lastBuildupChange = 0;
  /** When the placement was sent; see waitForConstructionSite(). */
  private buildOrderSince = 0;
  /** Items of the type that existed before the placement; see waitForConstructionSite(). */
  private knownItemIds = new Set<number>();
  private selectionListener: (() => void) | null = null;
  private placeConfigCenter: DecimalPosition | null = null;
  private placeConfigBoundaryRect: Rectangle | null = null;
  private markerConfig: MarkerConfig | null = null;

  constructor(
    private readonly toBeBuiltItemTypeId: number,
    private readonly placeConfig: PlaceConfig | null,
    tipService: TipService,
    tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  /**
   * Done when the site this task followed stands. Clicking "continue building" only sends a
   * command - counting that as done ended the whole tip chain on a construction site the player
   * then abandoned, leaving an open quest with no guidance at all and nothing in the tracking.
   * <p>
   * Deliberately about *this* site and not about any finished building of the type: the player
   * may well own one already, and on a "build another one" quest that would end the tip before
   * it ever pointed anywhere.
   */
  isFulfilled(): boolean {
    if (this.toBeFinalizedId === null) {
      return false;
    }
    const constructionSite = this.tipService.renderService.getBabylonBaseItemById(this.toBeFinalizedId);
    // Out of view and therefore not resolvable, or buildup not reported yet: unknown, not finished.
    const buildup = constructionSite?.getBuildupOrNull() ?? null;
    return buildup !== null && buildup >= 1.0;
  }

  getTaskName(): string {
    return TipTaskName.SEND_BUILD_COMMAND;
  }

  start(): void {
    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.selectionService.addSelectionListener(this.selectionListener);
    }

    this.markerConfig = this.getMarkerConfig();

    // A restart while a site is already being followed (scrolled back into view, actor
    // re-created) must stay on that site instead of asking for another placement.
    const followed = this.toBeFinalizedId === null
      ? null
      : this.tipService.renderService.getBabylonBaseItemById(this.toBeFinalizedId);
    const constructionSite = followed ?? this.findConstructionSite();
    if (constructionSite) {
      this.attachToConstructionSite(constructionSite);
    } else if (this.toBeFinalizedId !== null) {
      // Followed site is out of view right now - keep watching, do not fall back to the placer.
      this.stallReason = TipStallReason.TARGET_OUT_OF_VIEW;
      this.pollBuildup();
    } else {
      this.stallReason = TipStallReason.AWAIT_PLACEMENT;
      // Everything of this type that exists before the placement. Whatever turns up afterwards
      // is the new construction site - it cannot be recognised by its buildup, which is still
      // unreported at that moment and therefore reads as finished.
      this.knownItemIds = new Set(this.ownItemsOfType().map(item => item.getId()));
      // Setup place config visualization if configured
      if (this.placeConfig) {
        this.placeConfigBoundaryRect = this.calculatePlaceConfigBoundaryRect();
        this.placeConfigCenter = this.calculatePlaceConfigCenter();
        this.tipService.renderService.addViewFieldListener(this);
        this.updatePlaceVisualization(this.tipService.renderService.getCurrentViewField());
      }

      this.tipService.renderService.setBaseItemPlacerCallback((event) => {
        switch (event) {
          case BaseItemPlacerPresenterEvent.PLACED:
            // Unregister first: placing deactivates the placer, so DEACTIVATED follows within the
            // same click. Left registered it fails the task right after a successful placement,
            // and the backtrack-restart-fail loop that follows also keeps resetting the stall
            // watchdog - no tip and no telemetry either.
            this.tipService.renderService.setBaseItemPlacerCallback(null);
            // Placing starts the construction, it does not finish it. The site appears once the
            // builder has driven there and begun.
            this.stallReason = TipStallReason.AWAIT_BUILD_SITE;
            this.buildOrderSince = Date.now();
            this.waitForConstructionSite();
            break;
          case BaseItemPlacerPresenterEvent.DEACTIVATED:
            this.onFailed();
            break;
        }
      });
    }
  }

  /** Own items of the type the quest asks for, finished or not. */
  private ownItemsOfType(): BabylonBaseItemImpl[] {
    return this.tipService.renderService.getBabylonBaseItemsByDiplomacy(Diplomacy.OWN)
      .filter(item => item.itemType.getId() === this.toBeBuiltItemTypeId);
  }

  /** Reported as still going up. Unreported buildup does not count - see getBuildupOrNull(). */
  private findConstructionSite(): BabylonBaseItemImpl | null {
    return this.ownItemsOfType().find(item => {
      const buildup = item.getBuildupOrNull();
      return buildup !== null && buildup < 1.0;
    }) ?? null;
  }

  /**
   * After placing: wait for the item the server creates, then follow it until it stands.
   *
   * The new item is recognised by not having been there before the placement, not by its
   * buildup - that is still unreported for a moment after creation and reads as finished.
   */
  private waitForConstructionSite(): void {
    const constructionSite = this.findConstructionSite()
      ?? this.ownItemsOfType().find(item => !this.knownItemIds.has(item.getId()))
      ?? null;
    if (constructionSite) {
      this.attachToConstructionSite(constructionSite);
      return;
    }
    if (this.buildOrderAbandoned()) {
      // Nothing was built and nothing is coming: the tip has to go back to the button that
      // starts the placement, not keep waiting for a site that will never exist.
      this.onFailed();
      return;
    }
    this.buildupPollTimeout = setTimeout(() => this.waitForConstructionSite(),
      SendBuildCommandTipTask.BUILDUP_POLL_MILLIS);
  }

  /**
   * The site does not appear on placement - the builder drives there first and lays it down on
   * arrival. A move command in between drops the order, and then the builder stands idle with no
   * site anywhere: waiting for one meant an unreachable tip and a player with nothing to do.
   *
   * Idle is the signal rather than a timeout, because the drive there takes as long as it takes.
   * While the builder is out of view nothing is claimed - its state cannot be read out there.
   */
  private buildOrderAbandoned(): boolean {
    if (Date.now() - this.buildOrderSince < SendBuildCommandTipTask.BUILD_ORDER_SETTLE_MILLIS) {
      return false;
    }
    return this.tipTaskContext.babylonBaseItemImpl?.getIdle() === true;
  }

  private attachToConstructionSite(constructionSite: BabylonBaseItemImpl): void {
    this.toBeFinalized = constructionSite;
    this.toBeFinalizedId = constructionSite.getId();
    this.stallReason = TipStallReason.AWAIT_BUILD_FINALIZE;
    this.lastBuildup = constructionSite.getBuildupOrNull();
    this.lastBuildupChange = Date.now();
    const position = constructionSite.getPosition();
    if (position) {
      // Gives the player a way back to the site, and the return into view restarts the task.
      this.tipService.setOutOfViewTarget(GwtInstance.newDecimalPosition(position.getX(), position.getY()));
    }
    this.pollBuildup();
  }

  /**
   * Watches the site until it stands. Polling rather than setBuildupCallback: that is a single
   * slot the item cockpit takes over whenever the player selects the site, and the tip would
   * then never learn that its building was finished.
   */
  private pollBuildup(): void {
    this.buildupPollTimeout = setTimeout(() => {
      this.buildupPollTimeout = null;
      const site = this.toBeFinalizedId === null
        ? null
        : this.tipService.renderService.getBabylonBaseItemById(this.toBeFinalizedId);
      if (!site) {
        // Streamed out. The out-of-view marker still points at it, so keep watching.
        this.stallReason = TipStallReason.TARGET_OUT_OF_VIEW;
        this.pollBuildup();
        return;
      }
      if (site !== this.toBeFinalized) {
        this.toBeFinalized = site;
      }
      const buildup = site.getBuildupOrNull();
      // Unreported buildup means "not known yet", never "finished" - a site that has just been
      // created reports nothing for a moment, and taking that for done ended the tip on the spot.
      if (buildup !== null && buildup >= 1.0) {
        this.onSucceed();
        return;
      }
      if (buildup !== this.lastBuildup) {
        this.lastBuildup = buildup;
        this.lastBuildupChange = Date.now();
      }
      this.stallReason = this.constructionSiteReason(site);
      this.updateSitePrompt(site);
      this.demandSelectionForStalledSite();
      this.pollBuildup();
    }, SendBuildCommandTipTask.BUILDUP_POLL_MILLIS);
  }

  /**
   * "Click to continue building" is an instruction, so it may only be up while there is something
   * to do. A site going up needs nobody: the prompt sat on every building under construction and
   * told the player to act while the builder was doing exactly what it should.
   *
   * Asked of the item rather than inferred from a changed instance: the prompt goes down with the
   * site every time it leaves the view, and it does not always come back as a new object.
   */
  private updateSitePrompt(site: BabylonBaseItemImpl): void {
    if (this.siteStalled()) {
      if (!site.isSelectPromptVisible()) {
        site.showSelectPromptVisualization("Click to continue building", "200px", "250px");
      }
    } else if (site.isSelectPromptVisible()) {
      site.hideSelectPromptVisualization();
    }
  }

  /** Nothing has happened to the site for long enough that the builder is not coming back by itself. */
  private siteStalled(): boolean {
    return Date.now() - this.lastBuildupChange > SendBuildCommandTipTask.NO_PROGRESS_MILLIS;
  }

  /**
   * A site that stands still needs the builder sent back to it, and that click does nothing
   * unless the builder is selected. Falling back a step then puts the tip on the builder first -
   * the chain skips the placer on its way back, because a construction site of the type counts
   * as one built (StartBuildPlacerTipTask.isFulfilled()), so it lands right back here.
   *
   * State, not event: the placer clears the selection on its way out and a deselection while the
   * builder is out of view fires nothing at all, so there is no event this task could wait for.
   * Only once the site has stopped moving, though - while it is going up nothing is asked of the
   * player and demanding a selection would be noise.
   */
  private demandSelectionForStalledSite(): void {
    if (this.siteStalled() && this.isSelectionLost()) {
      this.onSelectionLost(() => this.isSelectionLost());
    }
  }

  private constructionSiteReason(constructionSite: BabylonBaseItemImpl): string {
    const position = constructionSite.getPosition();
    const viewField = this.tipService.renderService.getCurrentViewField();
    if (position && viewField
      && !viewField.contains(GwtInstance.newDecimalPosition(position.getX(), position.getY()))) {
      return TipStallReason.TARGET_OUT_OF_VIEW;
    }
    if (Date.now() - this.lastBuildupChange > SendBuildCommandTipTask.NO_PROGRESS_MILLIS) {
      return TipStallReason.BUILD_NOT_PROGRESSING;
    }
    return TipStallReason.AWAIT_BUILD_FINALIZE;
  }

  cleanup(): void {
    this.cancelSelectionLossGrace();
    if (this.buildupPollTimeout !== null) {
      clearTimeout(this.buildupPollTimeout);
      this.buildupPollTimeout = null;
    }
    this.tipService.setOutOfViewTarget(null);
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.renderService.removeViewFieldListener(this);
    this.tipService.renderService.showOutOfViewMarker(null, 0);
    this.tipService.renderService.setBaseItemPlacerCallback(null);
    this.tipService.renderService.showPlaceMarker(null, null);
    // Through the id, so a site that was re-created while the tip ran gets its prompt taken down
    // as well - the stale instance would take the prompt of a dead item with it and leave the
    // live one shouting.
    const constructionSite = this.toBeFinalizedId === null
      ? this.toBeFinalized
      : this.tipService.renderService.getBabylonBaseItemById(this.toBeFinalizedId) ?? this.toBeFinalized;
    if (constructionSite) {
      constructionSite.setItemClickCallback(null);
      constructionSite.hideSelectPromptVisualization();
    }
    this.toBeFinalized = null;
    this.toBeFinalizedId = null;
    this.buildOrderSince = 0;
  }

  onViewFieldChanged(viewField: ViewField): void {
    if (this.placeConfig && this.placeConfigCenter) {
      this.updatePlaceVisualization(viewField);
    }
  }

  private updatePlaceVisualization(viewField: ViewField): void {
    if (!this.placeConfigCenter || !this.markerConfig || !this.placeConfigBoundaryRect) {
      return;
    }

    const innerAabb = viewField.calculateInnerAabbRectangle();
    const outOfView = !this.placeConfigAdjoinsRect(innerAabb);

    if (outOfView) {
      // Show OutOfView, hide place marker
      const angle = viewField.getAngleTo(this.placeConfigCenter);
      this.tipService.renderService.showOutOfViewMarker(this.markerConfig, angle);
      this.tipService.renderService.showPlaceMarker(null, null);
    } else {
      // Show place marker, hide OutOfView
      this.tipService.renderService.showOutOfViewMarker(null, 0);
      this.tipService.renderService.showPlaceMarker(this.placeConfig, this.markerConfig);
    }
  }

  private onSelectionChanged(): void {
    if (!this.isSelectionLost()) {
      return;
    }
    this.onSelectionLost(() => this.isSelectionLost());
  }

  /**
   * While the placer runs the selection does not matter - placing is exactly the step this task
   * waits for, and the placer clears the selection on its way.
   */
  private isSelectionLost(): boolean {
    return !this.tipService.selectionService.hasOwnSelection()
      && !this.tipService.renderService.baseItemPlacerActive;
  }

  private getMarkerConfig(): MarkerConfig {
    const config = this.tipService.gwtAngularFacade.gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig();
    return {
      radius: config.getRadius(),
      nodesMaterialId: config.getNodesMaterialId(),
      placeNodesMaterialId: config.getPlaceNodesMaterialId(),
      outOfViewNodesMaterialId: config.getOutOfViewNodesMaterialId(),
      outOfViewSize: config.getOutOfViewSize(),
      outOfViewDistanceFromCamera: config.getOutOfViewDistanceFromCamera()
    };
  }

  private calculatePlaceConfigCenter(): DecimalPosition | null {
    if (!this.placeConfigBoundaryRect) {
      return null;
    }
    const rect = this.placeConfigBoundaryRect;
    return GwtInstance.newDecimalPosition(
      rect.x + rect.width / 2,
      rect.y + rect.height / 2
    );
  }

  private calculatePlaceConfigBoundaryRect(): Rectangle | null {
    if (!this.placeConfig) {
      return null;
    }

    if (this.placeConfig.getPosition()) {
      const pos = this.placeConfig.getPosition()!;
      const radius = this.placeConfig.toRadiusAngular() || 1;
      return {
        x: pos.getX() - radius,
        y: pos.getY() - radius,
        width: radius * 2,
        height: radius * 2
      };
    } else if (this.placeConfig.getPolygon2D()) {
      const corners = this.placeConfig.getPolygon2D()!.toCornersAngular();
      let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
      for (const corner of corners) {
        minX = Math.min(minX, corner.getX());
        minY = Math.min(minY, corner.getY());
        maxX = Math.max(maxX, corner.getX());
        maxY = Math.max(maxY, corner.getY());
      }
      return {
        x: minX,
        y: minY,
        width: maxX - minX,
        height: maxY - minY
      };
    }

    return null;
  }

  private placeConfigAdjoinsRect(rect: Rectangle): boolean {
    if (!this.placeConfig) {
      return false;
    }

    if (this.placeConfig.getPosition()) {
      const pos = this.placeConfig.getPosition()!;
      const radius = this.placeConfig.toRadiusAngular();
      if (radius) {
        // Check if circle adjoins rectangle
        return this.rectAdjoinsCircle(rect, pos.getX(), pos.getY(), radius);
      } else {
        // Check if point is inside rectangle
        return this.rectContainsPoint(rect, pos.getX(), pos.getY());
      }
    } else if (this.placeConfig.getPolygon2D()) {
      // Check if polygon adjoins rectangle
      const corners = this.placeConfig.getPolygon2D()!.toCornersAngular();
      // Check if any corner is inside the rectangle
      for (const corner of corners) {
        if (this.rectContainsPoint(rect, corner.getX(), corner.getY())) {
          return true;
        }
      }
      // Check if any rectangle corner is inside the polygon (simplified check)
      const rectCorners = [
        {x: rect.x, y: rect.y},
        {x: rect.x + rect.width, y: rect.y},
        {x: rect.x + rect.width, y: rect.y + rect.height},
        {x: rect.x, y: rect.y + rect.height}
      ];
      for (const rc of rectCorners) {
        if (this.pointInPolygon(rc.x, rc.y, corners)) {
          return true;
        }
      }
      return false;
    }

    return false;
  }

  private rectAdjoinsCircle(rect: Rectangle, cx: number, cy: number, radius: number): boolean {
    // Find the closest point on the rectangle to the circle center
    const closestX = Math.max(rect.x, Math.min(cx, rect.x + rect.width));
    const closestY = Math.max(rect.y, Math.min(cy, rect.y + rect.height));
    const distanceSquared = (closestX - cx) * (closestX - cx) + (closestY - cy) * (closestY - cy);
    return distanceSquared < radius * radius;
  }

  private rectContainsPoint(rect: Rectangle, x: number, y: number): boolean {
    return x >= rect.x && x <= rect.x + rect.width && y >= rect.y && y <= rect.y + rect.height;
  }

  private pointInPolygon(x: number, y: number, corners: DecimalPosition[]): boolean {
    let inside = false;
    for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
      const xi = corners[i].getX(), yi = corners[i].getY();
      const xj = corners[j].getX(), yj = corners[j].getY();
      if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
        inside = !inside;
      }
    }
    return inside;
  }
}
