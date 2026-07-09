import {HttpClient} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {MessageService} from 'primeng/api';
import {TooltipModule} from 'primeng/tooltip';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {getImageUrl} from 'src/app/common';
import {LevelConfig, TechTreeControllerClient} from 'src/app/generated/razarion-share';
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {CockpitDisplayService} from '../cockpit-display.service';

/** One level's allowance for a unit: the base level grant plus crystal-unlockable extras. */
interface TechTreeCell {
  /** Units granted by the level itself. */
  base: number;
  /** Additional units unlockable with crystals, accumulated up to this level (unlocks persist). */
  unlock: number;
  /** Crystal-unlockable units that become newly available on this very level. */
  newUnlock: number;
  /** base + unlock. */
  total: number;
}

/** One unit (row): its allowance on every level (parallel to {@link TechTreeComponent.levels}). */
interface TechTreeRow {
  itemTypeId: number;
  name: string;
  imageUrl: string;
  counts: TechTreeCell[];
}

/**
 * A read-only teaser matrix: columns = levels, rows = units, cell = how many of that
 * unit the player is allowed at that level. Data comes from the player-facing tech tree
 * endpoint (accessible to non-admins); unit names/thumbnails are resolved through the
 * in-game item type service.
 */
@Component({
  selector: 'tech-tree',
  templateUrl: './tech-tree.component.html',
  imports: [TooltipModule],
  styleUrls: ['./tech-tree.component.scss']
})
export class TechTreeComponent implements OnInit {
  levels: LevelConfig[] = [];
  rows: TechTreeRow[] = [];
  loading = true;
  loadFailed = false;
  private techTreeControllerClient: TechTreeControllerClient;

  constructor(httpClient: HttpClient,
              private gwtAngularService: GwtAngularService,
              private messageService: MessageService,
              public cockpitDisplayService: CockpitDisplayService) {
    this.techTreeControllerClient = new TechTreeControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.techTreeControllerClient.readLevels().then(levels => {
      this.buildMatrix(levels);
      this.loading = false;
    }).catch((reason: any) => {
      this.loading = false;
      this.loadFailed = true;
      this.messageService.add({
        severity: 'error',
        summary: 'Failed to load tech tree',
        detail: reason,
        sticky: true
      });
    });
  }

  private buildMatrix(levels: LevelConfig[]): void {
    this.levels = [...levels].sort((a, b) => a.number - b.number);

    // Resolve display name + thumbnail for every base item type known to the running game.
    const itemTypeService = this.gwtAngularService.gwtAngularFacade.itemTypeService;
    const knownById = new Map<number, { name: string, thumbnail: number | null }>();
    itemTypeService.getAllBaseItemTypes().forEach(baseItemType => {
      knownById.set(baseItemType.getId(), {name: baseItemType.getName(), thumbnail: baseItemType.getThumbnail()});
    });

    // Per-level crystal-unlock delta (what becomes newly unlockable on that level) and the
    // running accumulation (unlocks persist across levels).
    const unlockByLevel: Map<number, number>[] = [];
    const newUnlockByLevel: Map<number, number>[] = [];
    const runningUnlocks = new Map<number, number>();
    this.levels.forEach(level => {
      const delta = new Map<number, number>();
      (level.levelUnlockConfigs || []).forEach(unlock => {
        if (unlock.baseItemType != null) {
          delta.set(unlock.baseItemType, (delta.get(unlock.baseItemType) || 0) + unlock.baseItemTypeCount);
          runningUnlocks.set(unlock.baseItemType, (runningUnlocks.get(unlock.baseItemType) || 0) + unlock.baseItemTypeCount);
        }
      });
      newUnlockByLevel.push(delta);
      unlockByLevel.push(new Map(runningUnlocks));
    });

    // For each unit remember the first level it becomes available on (via base grant OR unlock),
    // so rows read as a progression.
    const firstLevelIndex = new Map<number, number>();
    this.levels.forEach((level, levelIndex) => {
      const limitation = level.itemTypeLimitation || {};
      Object.keys(limitation).forEach(key => {
        const itemTypeId = Number(key);
        if (limitation[key] > 0 && !firstLevelIndex.has(itemTypeId)) {
          firstLevelIndex.set(itemTypeId, levelIndex);
        }
      });
      unlockByLevel[levelIndex].forEach((count, itemTypeId) => {
        if (count > 0 && !firstLevelIndex.has(itemTypeId)) {
          firstLevelIndex.set(itemTypeId, levelIndex);
        }
      });
    });

    const rows: TechTreeRow[] = [];
    firstLevelIndex.forEach((_firstIndex, itemTypeId) => {
      const known = knownById.get(itemTypeId);
      if (!known) {
        return; // Item type no longer exists in the running game – skip it.
      }
      rows.push({
        itemTypeId,
        name: known.name,
        imageUrl: known.thumbnail != null ? getImageUrl(known.thumbnail) : '',
        counts: this.levels.map((level, levelIndex) => {
          const base = (level.itemTypeLimitation || {})[itemTypeId] || 0;
          const unlock = unlockByLevel[levelIndex].get(itemTypeId) || 0;
          const newUnlock = newUnlockByLevel[levelIndex].get(itemTypeId) || 0;
          return {base, unlock, newUnlock, total: base + unlock};
        })
      });
    });

    rows.sort((a, b) => {
      const firstA = firstLevelIndex.get(a.itemTypeId)!;
      const firstB = firstLevelIndex.get(b.itemTypeId)!;
      return firstA !== firstB ? firstA - firstB : a.name.localeCompare(b.name);
    });
    this.rows = rows;
  }

  isCurrentLevel(level: LevelConfig): boolean {
    return level.number === this.cockpitDisplayService.currentLevelNumber;
  }
}
