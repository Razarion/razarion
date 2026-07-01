import {AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
  AbstractMesh,
  ArcRotateCamera,
  Color3,
  Color4,
  DirectionalLight,
  GizmoManager,
  Mesh,
  PointerEventTypes,
  Ray,
  Scene,
  ShadowGenerator,
  Tools,
  TransformNode,
  Vector3
} from '@babylonjs/core';
import {StudioSceneSummary} from '../../../../../src/app/generated/razarion-share';
import {BabylonModelService} from '../../../../../src/app/game/renderer/babylon-model.service';
import {BabylonBuildupEffect} from '../../../../../src/app/game/renderer/babylon-buildup-effect';
import {BabylonExplosion} from '../../../../../src/app/game/renderer/babylon-explosion';
import {BabylonHarvestingBeam} from '../../../../../src/app/game/renderer/babylon-harvesting-beam';
import {BabylonImpact} from '../../../../../src/app/game/renderer/babylon-impact';
import {BabylonLightning} from '../../../../../src/app/game/renderer/babylon-lightning';
import {BabylonMuzzleFlash} from '../../../../../src/app/game/renderer/babylon-muzzle-flash';
import {BabylonResourceDecal} from '../../../../../src/app/game/renderer/babylon-resource-decal';
import {BabylonResourceSparkle} from '../../../../../src/app/game/renderer/babylon-resource-sparkle';
import {BabylonWreckage} from '../../../../../src/app/game/renderer/babylon-wreckage';
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';
import {RenderObject} from '../../../../../src/app/game/renderer/render-object';
import {Diplomacy} from '../../../../../src/app/gwtangular/GwtAngularFacade';
import {ObjectNameId} from '../../../../../src/app/generated/razarion-share';
import {SceneContent, SceneItem, SceneStorageService, SceneTerrain, emptyScene} from './scene-storage.service';
import {TerrainLoaderService} from './terrain-loader.service';
import {ThumbnailItem, ThumbnailStorageService} from './thumbnail-storage.service';

const DIPLOMACIES: Diplomacy[] = [Diplomacy.OWN, Diplomacy.ENEMY, Diplomacy.FRIEND, Diplomacy.RESOURCE, Diplomacy.BOX];

export type GizmoTool = 'move' | 'rotate' | 'scale' | 'none';

/**
 * Three-pane scene composer: Scene list · Babylon viewport · Library/Properties.
 *
 * Items spawn at origin on library click; clicking a placed mesh selects it and
 * attaches a Babylon GizmoManager so the user can move/rotate/scale via gizmos
 * or via the numeric inputs in the Properties pane. Save flushes the scene-node
 * transforms back into content.items[] before POST.
 *
 * Renderer is on-demand — Babylon engine spins up the first time a scene is
 * opened and stays warm for subsequent opens within the session.
 */
@Component({
  selector: 'studio-scene-composer-task',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="composer">
      <!-- ============ LEFT: scene list ============ -->
      <aside class="left">
        <header>
          <h3>Scenes</h3>
          <button class="ghost sm" (click)="reload()" [disabled]="loading()" title="Re-fetch scene list">↻</button>
        </header>

        @if (!hasToken()) {
          <div class="status warn">Not logged in — use the header form.</div>
        }
        @if (storage.lastError(); as err) {
          <div class="status error">{{ err }}</div>
        }

        <ul class="scene-list">
          @for (s of storage.scenes(); track s.id) {
            <li [class.active]="current()?.id === s.id" (click)="openScene(s.id)">
              <span class="scene-name">{{ s.name }}</span>
              <button class="ghost xs" (click)="deleteScene(s, $event)" title="Delete">×</button>
            </li>
          } @empty {
            <li class="empty">No scenes yet.</li>
          }
        </ul>

        <button class="primary block" (click)="createScene()" [disabled]="busy()">+ New scene</button>
      </aside>

      <!-- ============ CENTER: viewport ============ -->
      <section class="viewport">
        @if (current()) {
          <div class="viewport-header">
            <input class="scene-rename" type="text" [(ngModel)]="currentName" [disabled]="busy()">
            <div class="tool-palette" role="group" aria-label="Transform tool">
              <button [class.active]="gizmoTool() === 'move'" (click)="setGizmoTool('move')" title="Move (G)">↔</button>
              <button [class.active]="gizmoTool() === 'rotate'" (click)="setGizmoTool('rotate')" title="Rotate (R)">↻</button>
              <button [class.active]="gizmoTool() === 'scale'" (click)="setGizmoTool('scale')" title="Scale (S)">⤡</button>
              <button [class.active]="gizmoTool() === 'none'" (click)="setGizmoTool('none')" title="Hide gizmo, keep selection (H)">∅</button>
            </div>
            <div class="spacer"></div>
            <button class="primary" (click)="saveScene()" [disabled]="busy() || !!rendererStatus()">
              {{ busy() ? 'Saving…' : 'Save' }}
            </button>
          </div>
        } @else {
          <div class="placeholder">
            <p>Pick a scene from the left or click <strong>+ New scene</strong>.</p>
          </div>
        }
        <canvas #viewportCanvas [hidden]="!current()"></canvas>
        @if (current() && rendererStatus(); as msg) {
          <div class="loading-overlay">
            <div class="spinner"></div>
            <span>{{ msg }}</span>
          </div>
        }
      </section>

      <!-- ============ RIGHT: library + properties ============ -->
      <aside class="right">
        @if (current()) {
          <header><h3>Scene</h3></header>
          <div class="scene-settings">
            <div class="prop-row">
              <span class="prop-label">Planet</span>
              <select (change)="onPlanetChange($event)" [disabled]="busy()">
                <option value="" [selected]="content()?.terrain?.planetId == null">— none —</option>
                @for (p of planets(); track p.id) {
                  <option [value]="p.id" [selected]="p.id === content()?.terrain?.planetId">{{ p.internalName }} (#{{ p.id }})</option>
                }
              </select>
            </div>
            @if (content()?.terrain?.planetId) {
              <div class="prop-row">
                <span class="prop-label">Terrain area</span>
                <select (change)="onRegionModeChange($event)" [disabled]="busy()">
                  <option value="auto" [selected]="regionMode() === 'auto'">Around camera (fast)</option>
                  <option value="custom" [selected]="regionMode() === 'custom'">Custom region</option>
                  <option value="full" [selected]="regionMode() === 'full'">Full map (slow)</option>
                </select>
              </div>
              @if (content()?.terrain?.region; as r) {
                <div class="prop-row">
                  <span class="prop-label">X / Y</span>
                  <input type="number" step="10" [ngModel]="r.x" (ngModelChange)="updateRegion('x', $event)">
                  <input type="number" step="10" [ngModel]="r.y" (ngModelChange)="updateRegion('y', $event)">
                </div>
                <div class="prop-row">
                  <span class="prop-label">W / H</span>
                  <input type="number" step="10" min="10" [ngModel]="r.w" (ngModelChange)="updateRegion('w', $event)">
                  <input type="number" step="10" min="10" [ngModel]="r.h" (ngModelChange)="updateRegion('h', $event)">
                </div>
                <div class="prop-row">
                  <button (click)="reloadTerrain()" [disabled]="busy()">Apply region</button>
                  <button class="ghost" (click)="centerRegionOnCamera()" [disabled]="busy()">Center on camera</button>
                </div>
              }
            }
          </div>

          <div class="divider"></div>

          <header><h3>Render</h3></header>
          <div class="scene-settings">
            <div class="prop-row">
              <span class="prop-label">Resolution</span>
              <select [(ngModel)]="screenshotPreset">
                @for (p of screenshotPresets; track p.label) {
                  <option [ngValue]="p">{{ p.label }}</option>
                }
              </select>
            </div>
            @if (screenshotPreset.w === 0) {
              <div class="prop-row">
                <span class="prop-label">Width × Height</span>
                <input type="number" min="64" step="16" [(ngModel)]="customWidth">
                <input type="number" min="64" step="16" [(ngModel)]="customHeight">
              </div>
            }
            <div class="prop-row">
              <span class="prop-label">Background</span>
              <select [(ngModel)]="screenshotBackground">
                <option value="transparent">Transparent PNG</option>
                <option value="scene">Scene background</option>
              </select>
            </div>
            <div class="prop-row">
              <button (click)="takeScreenshot()" [disabled]="!rendererReady() || screenshotBusy()">
                {{ screenshotBusy() ? 'Rendering…' : 'Take screenshot' }}
              </button>
            </div>
          </div>

          <div class="divider"></div>

          <header>
            <h3>Library</h3>
            <input class="filter" type="search" placeholder="filter…" [(ngModel)]="libraryFilter">
          </header>
          <div class="library">
            @for (it of filteredLibrary(); track itemKey(it)) {
              <button class="lib-item"
                      (click)="spawnFromLibrary(it)"
                      [disabled]="it.model3DId === null"
                      [title]="it.model3DId === null ? 'No model3D' : 'Click to spawn at origin'">
                @if (it.thumbnailImageId !== null) {
                  <img [src]="'/rest/image/' + it.thumbnailImageId" [alt]="it.internalName">
                } @else {
                  <div class="no-img">{{ it.kind[0].toUpperCase() }}</div>
                }
                <span class="lib-name">{{ it.internalName }}</span>
              </button>
            } @empty {
              <div class="empty">No items match.</div>
            }
          </div>

          <div class="divider"></div>

          <header>
            <h3>Properties</h3>
            @if (selectedItem()) {
              <button class="ghost" (click)="deselect()" title="Esc">Deselect</button>
            }
          </header>
          @if (selectedItem(); as it) {
            <div class="props">
              <div class="prop-row">
                <span class="prop-label">Item</span>
                <span class="prop-value">{{ libraryLabel(it) }} <span class="muted">#{{ it.id }}</span></span>
              </div>
              <div class="prop-row">
                <span class="prop-label">Position</span>
                <input type="number" step="0.5" [ngModel]="it.position[0]" (ngModelChange)="updateProp('px', $event)">
                <input type="number" step="0.5" [ngModel]="it.position[1]" (ngModelChange)="updateProp('py', $event)">
                <input type="number" step="0.5" [ngModel]="it.position[2]" (ngModelChange)="updateProp('pz', $event)">
              </div>
              <div class="prop-row">
                <span class="prop-label">Rotation Y</span>
                <input type="number" step="0.1" [ngModel]="it.rotationY" (ngModelChange)="updateProp('ry', $event)">
                <span class="muted">rad</span>
              </div>
              <div class="prop-row">
                <span class="prop-label">Scale</span>
                <input type="number" step="0.1" min="0.1" [ngModel]="it.scale" (ngModelChange)="updateProp('s', $event)">
              </div>
              <div class="prop-row">
                <span class="prop-label">Diplomacy</span>
                <select [ngModel]="it.diplomacy" (ngModelChange)="updateProp('diplomacy', $event)">
                  @for (d of diplomacies; track d) {
                    <option [value]="d">{{ d }}</option>
                  }
                </select>
              </div>
              <div class="prop-row">
                <span class="prop-label">Attack target</span>
                <select [ngModel]="it.attackTargetId ?? ''" (ngModelChange)="updateProp('attackTargetId', $event)">
                  <option value="">— none —</option>
                  @for (other of otherItems(it.id); track other.id) {
                    <option [value]="other.id">{{ libraryLabel(other) }} #{{ other.id }}</option>
                  }
                </select>
              </div>
              @if (it.attackTargetId != null) {
                <div class="prop-row">
                  <button (click)="fireAttack(it.id)">Fire once</button>
                  <label class="muted" style="display:flex;align-items:center;gap:4px;">
                    <input type="checkbox" [checked]="isLooping(it.id)" (change)="toggleAttackLoop(it.id)">
                    Loop
                  </label>
                  <label class="muted" style="display:flex;align-items:center;gap:4px;">
                    <input type="checkbox" [checked]="it.explodeTargetOnFire === true"
                           (change)="updateProp('explodeTargetOnFire', $any($event.target).checked)">
                    Explode target
                  </label>
                </div>
                <div class="prop-row">
                  <button (click)="fireAndCapture(it.id)"
                          [disabled]="!rendererReady() || screenshotBusy()"
                          title="Fire attack, then auto-screenshot after the delay">
                    Fire + capture
                  </button>
                  <label class="muted" style="display:flex;align-items:center;gap:4px;">
                    Delay
                    <input type="number" min="0" step="10" [(ngModel)]="captureDelayMs" style="width:64px;">
                    ms
                  </label>
                </div>
              }
              <div class="prop-row">
                <span class="prop-label">Harvest target</span>
                <select [ngModel]="it.harvestTargetId ?? ''" (ngModelChange)="updateProp('harvestTargetId', $event)">
                  <option value="">— none —</option>
                  @for (res of harvestTargets(it.id); track res.id) {
                    <option [value]="res.id">{{ libraryLabel(res) }} #{{ res.id }}</option>
                  }
                </select>
              </div>
              @if (it.harvestTargetId != null) {
                <div class="prop-row">
                  <button (click)="toggleHarvest(it.id)">
                    {{ isHarvesting(it.id) ? 'Stop harvest' : 'Start harvest' }}
                  </button>
                </div>
              }
              <div class="prop-row">
                <span class="prop-label">Build target</span>
                <select [ngModel]="it.buildTargetId ?? ''" (ngModelChange)="updateProp('buildTargetId', $event)">
                  <option value="">— none —</option>
                  @for (b of buildTargets(it.id); track b.id) {
                    <option [value]="b.id">{{ libraryLabel(b) }} #{{ b.id }}</option>
                  }
                </select>
              </div>
              @if (it.buildTargetId != null) {
                <div class="prop-row">
                  <button (click)="toggleBuild(it.id)">
                    {{ isBuilding(it.id) ? 'Stop build' : 'Start build' }}
                  </button>
                </div>
              }
              <div class="prop-row">
                <span class="prop-label">Fabricate type</span>
                <select [ngModel]="it.fabricateTypeId ?? ''" (ngModelChange)="updateProp('fabricateTypeId', $event)">
                  <option value="">— none —</option>
                  @for (t of fabricateTypes(); track t.id) {
                    <option [value]="t.id">{{ t.internalName }}</option>
                  }
                </select>
              </div>
              @if (it.fabricateTypeId != null) {
                <div class="prop-row">
                  <button (click)="toggleFabricate(it.id)">
                    {{ isFabricating(it.id) ? 'Stop fabricate' : 'Start fabricate' }}
                  </button>
                </div>
              }
              <div class="prop-row">
                <button (click)="explodeItem(it.id, true)">Explode now</button>
                <button class="ghost" (click)="deleteSelected()">Delete item</button>
              </div>
            </div>
          } @else {
            <div class="placeholder small">
              <p>Click a placed item in the viewport to edit it.</p>
              <p class="muted">items: {{ content()?.items?.length ?? 0 }}</p>
            </div>
          }
        }
      </aside>

      <!-- ============ Screenshot preview modal ============ -->
      @if (screenshotPreview(); as p) {
        <div class="modal-backdrop" (click)="discardScreenshot()">
          <div class="modal" (click)="$event.stopPropagation()">
            <header class="modal-header">
              <h3>Screenshot preview</h3>
              <span class="muted">{{ p.w }} × {{ p.h }}</span>
            </header>
            <div class="modal-body">
              <img [src]="p.dataUrl" [alt]="p.filename">
            </div>
            <footer class="modal-footer">
              <span class="muted filename">{{ p.filename }}</span>
              <button class="ghost" (click)="discardScreenshot()" title="Esc">Discard</button>
              <button class="primary" (click)="saveScreenshot()">Save PNG</button>
            </footer>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    :host { position: absolute; inset: 0; display: block; }
    .composer {
      position: absolute;
      inset: 0;
      display: grid;
      grid-template-columns: 220px 1fr 280px;
      gap: 1px;
      background: #2a2e35;
    }
    aside, .viewport {
      background: #15171c;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    aside header, .viewport-header {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-bottom: 1px solid #2a2e35;
      gap: 8px;
      flex-shrink: 0;
    }
    aside h3 {
      margin: 0;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #aaa;
    }
    .scene-list {
      list-style: none;
      margin: 0;
      padding: 6px 0;
      flex: 1;
      overflow-y: auto;
    }
    .scene-list li {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 7px 12px;
      font-size: 12px;
      color: #ddd;
      cursor: pointer;
      border-left: 3px solid transparent;
    }
    .scene-list li:hover { background: #1c1f25; }
    .scene-list li.active { background: #232830; border-left-color: #4a9eff; }
    .scene-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .scene-list li.empty { color: #666; font-style: italic; cursor: default; }
    .scene-list li.empty:hover { background: transparent; }
    button {
      padding: 6px 12px;
      border-radius: 4px;
      border: 1px solid #4a9eff;
      background: rgba(74, 158, 255, 0.18);
      color: #eee;
      cursor: pointer;
      font-size: 12px;
    }
    button:hover:not(:disabled) { background: rgba(74, 158, 255, 0.32); }
    button.primary { background: rgba(74, 158, 255, 0.4); }
    button.primary:hover:not(:disabled) { background: rgba(74, 158, 255, 0.55); }
    button.ghost { background: transparent; border-color: rgba(255, 255, 255, 0.2); color: #aaa; }
    button.sm { padding: 3px 8px; font-size: 11px; }
    button.xs { padding: 0 6px; font-size: 14px; line-height: 1; }
    button.block { margin: 8px 12px; }
    button:disabled { opacity: 0.5; cursor: default; }
    .status {
      margin: 8px 12px;
      padding: 8px 10px;
      border-radius: 4px;
      font-size: 11px;
    }
    .status.warn { background: rgba(255, 200, 80, 0.12); color: #ffd56b; border: 1px solid rgba(255, 200, 80, 0.3); }
    .status.error { background: rgba(255, 90, 90, 0.12); color: #ff8a8a; border: 1px solid rgba(255, 90, 90, 0.3); }

    .viewport { background: #0e1014; position: relative; }
    .viewport canvas { flex: 1; width: 100%; height: 100%; display: block; outline: none; }
    .viewport-header {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      z-index: 10;
      background: rgba(20, 22, 26, 0.92);
    }
    .scene-rename {
      padding: 5px 8px;
      font-size: 13px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
      min-width: 200px;
    }
    .scene-rename:focus { outline: none; border-color: #4a9eff; }
    .viewport-header .spacer { flex: 1; }
    .tool-palette {
      display: inline-flex;
      border: 1px solid #4a9eff;
      border-radius: 4px;
      overflow: hidden;
      margin-left: 8px;
    }
    .tool-palette button {
      padding: 5px 10px;
      border: none;
      border-radius: 0;
      background: transparent;
      color: #aaa;
      font-size: 14px;
      line-height: 1;
      cursor: pointer;
      min-width: 28px;
    }
    .tool-palette button.active {
      background: rgba(74, 158, 255, 0.4);
      color: #fff;
    }
    .tool-palette button:not(.active):hover {
      background: rgba(74, 158, 255, 0.12);
    }
    .loading-overlay {
      position: absolute;
      inset: 0;
      z-index: 20;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 14px;
      background: rgba(14, 16, 20, 0.85);
      color: #ccc;
      font-size: 13px;
      letter-spacing: 0.04em;
    }
    .spinner {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      border: 3px solid rgba(74, 158, 255, 0.2);
      border-top-color: #4a9eff;
      animation: spin 0.9s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    .placeholder {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #777;
      font-size: 13px;
      padding: 30px;
      text-align: center;
    }
    .placeholder.small { padding: 12px; font-size: 11px; align-items: flex-start; }
    .placeholder p { margin: 4px 0; }
    .placeholder .muted, .muted { color: #555; font-size: 10px; }

    .filter {
      flex: 1;
      padding: 4px 8px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
    }
    .filter:focus { outline: none; border-color: #4a9eff; }
    .library {
      flex: 1;
      overflow-y: auto;
      padding: 4px 8px;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 4px;
      min-height: 100px;
    }
    .library .empty { grid-column: 1 / -1; color: #666; font-style: italic; padding: 12px; font-size: 11px; }
    .lib-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;
      padding: 4px;
      border-radius: 3px;
      border: 1px solid rgba(255, 255, 255, 0.08);
      background: rgba(255, 255, 255, 0.03);
      cursor: pointer;
      font-size: 10px;
    }
    .lib-item:hover:not(:disabled) { border-color: #4a9eff; background: rgba(74, 158, 255, 0.12); }
    .lib-item img { width: 56px; height: 56px; object-fit: contain; image-rendering: auto; }
    .lib-item .no-img {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #1a1d22;
      color: #555;
      font-weight: bold;
    }
    .lib-name {
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #ccc;
    }
    .divider { height: 1px; background: #2a2e35; margin: 4px 0; }
    .props {
      padding: 8px 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      font-size: 11px;
    }
    .prop-row {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
    }
    .prop-label {
      flex: 0 0 70px;
      color: #aaa;
      font-weight: 500;
    }
    .prop-value { color: #ddd; font-size: 11px; }
    .props input, .props select {
      flex: 1;
      min-width: 50px;
      padding: 3px 6px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-variant-numeric: tabular-nums;
      font-family: inherit;
    }
    .props input:focus, .props select:focus { outline: none; border-color: #4a9eff; }
    .scene-settings { padding: 8px 12px; display: flex; flex-direction: column; gap: 6px; font-size: 11px; }
    .scene-settings select {
      flex: 1;
      padding: 3px 6px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.18);
      border-radius: 3px;
      color: #eee;
      font-family: inherit;
    }
    .scene-settings select:focus { outline: none; border-color: #4a9eff; }

    .modal-backdrop {
      position: fixed;
      inset: 0;
      z-index: 100;
      background: rgba(0, 0, 0, 0.65);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
    }
    .modal {
      background: #15171c;
      border: 1px solid #2a2e35;
      border-radius: 6px;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6);
      display: flex;
      flex-direction: column;
      max-width: calc(100vw - 48px);
      max-height: calc(100vh - 48px);
      overflow: hidden;
    }
    .modal-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 16px;
      border-bottom: 1px solid #2a2e35;
      gap: 12px;
    }
    .modal-header h3 {
      margin: 0;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #ddd;
    }
    .modal-body {
      flex: 1;
      overflow: auto;
      padding: 16px;
      background:
        linear-gradient(45deg, #1a1d22 25%, transparent 25%),
        linear-gradient(-45deg, #1a1d22 25%, transparent 25%),
        linear-gradient(45deg, transparent 75%, #1a1d22 75%),
        linear-gradient(-45deg, transparent 75%, #1a1d22 75%);
      background-size: 20px 20px;
      background-position: 0 0, 0 10px, 10px -10px, -10px 0;
      background-color: #0e1014;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .modal-body img {
      max-width: 100%;
      max-height: calc(100vh - 200px);
      display: block;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
    }
    .modal-footer {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 16px;
      border-top: 1px solid #2a2e35;
    }
    .modal-footer .filename {
      flex: 1;
      font-family: monospace;
      color: #888;
      font-size: 11px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  `]
})
export class SceneComposerTaskComponent implements OnInit, AfterViewInit {
  protected readonly storage = inject(SceneStorageService);
  protected readonly thumbStorage = inject(ThumbnailStorageService);
  private readonly babylonModel = inject(BabylonModelService);
  private readonly babylonRender = inject(BabylonRenderServiceAccessImpl);
  private readonly terrainLoader = inject(TerrainLoaderService);

  @ViewChild('viewportCanvas', {static: true}) private canvasRef!: ElementRef<HTMLCanvasElement>;

  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly current = signal<StudioSceneSummary | null>(null);
  readonly content = signal<SceneContent | null>(null);
  readonly selectedItemId = signal<number | null>(null);
  readonly rendererStatus = signal<string>('');
  readonly gizmoTool = signal<GizmoTool>('move');
  readonly diplomacies = DIPLOMACIES;
  readonly planets = signal<ObjectNameId[]>([]);
  currentName = '';
  libraryFilter = '';

  /** Side length (world units) of the default terrain window loaded around the
   *  camera target when a scene has no explicit region. 960 = 6 tiles → 36
   *  tiles instead of the full planet's 1024. */
  private static readonly DEFAULT_REGION_SIZE = 960;
  /** When true, load the whole planet (slow) instead of a clipped region.
   *  Transient debug escape — never persisted with the scene. */
  readonly loadFullMap = signal(false);

  readonly screenshotPresets: ReadonlyArray<{label: string; w: number; h: number}> = [
    {label: '1920 × 1080 (FHD landscape)', w: 1920, h: 1080},
    {label: '1080 × 1920 (FHD portrait)', w: 1080, h: 1920},
    {label: '1080 × 1080 (social square)', w: 1080, h: 1080},
    {label: '2560 × 1440 (QHD)', w: 2560, h: 1440},
    {label: '3840 × 2160 (4K)', w: 3840, h: 2160},
    {label: 'Custom…', w: 0, h: 0}
  ];
  screenshotPreset = this.screenshotPresets[0];
  customWidth = 1920;
  customHeight = 1080;
  screenshotBackground: 'transparent' | 'scene' = 'transparent';
  /** Delay between fireAttack() and takeScreenshot() in "Fire + capture" mode.
   *  70ms = BabylonLightning bolt peak (PEAK_MS in babylon-lightning.ts). */
  captureDelayMs = 600;
  readonly screenshotBusy = signal(false);
  /** Pending screenshot awaiting Save/Discard decision in the preview modal.
   *  Set by takeScreenshot(); consumed by saveScreenshot()/discardScreenshot(). */
  readonly screenshotPreview = signal<{dataUrl: string; w: number; h: number; filename: string} | null>(null);
  readonly rendererReady = signal(false);

  private camera: ArcRotateCamera | null = null;
  private gizmoManager: GizmoManager | null = null;
  private readonly nodes = new Map<number, TransformNode>();
  private readonly renderObjects = new Map<number, RenderObject>();
  private nextItemId = 1;
  /** Item ids whose attack VFX is auto-replayed on a timer. Volatile — never
   *  persisted because a saved scene should always load idle. */
  private readonly attackLoops = new Map<number, ReturnType<typeof setInterval>>();
  /** Bump this to force template re-eval of isLooping(). */
  private readonly loopTick = signal(0);
  /** Active harvest beams keyed by harvester item id. Created on toggleHarvest,
   *  disposed when the harvester stops, the target is cleared, or the item is
   *  removed. Volatile — never persisted; a saved scene always loads idle. */
  private readonly harvestingBeams = new Map<number, BabylonHarvestingBeam>();
  /** Bump this to force template re-eval of isHarvesting(). */
  private readonly harvestTick = signal(0);
  /** Per-resource VFX so the studio mirrors the in-game look: a ground decal
   *  under the rock + the floating blue-white sparkle particles. Spawned in
   *  spawnNode for kind==='resource', re-built when scale (== radius proxy)
   *  changes, disposed in disposeItem. */
  private readonly resourceDecals = new Map<number, BabylonResourceDecal>();
  private readonly resourceSparkles = new Map<number, BabylonResourceSparkle>();
  /** Last spawned radius per resource — used to detect scale changes in
   *  applyToNode so position-only updates don't restart the sparkle particles. */
  private readonly resourceRadii = new Map<number, number>();
  /** Active build effects keyed by builder item id. Each entry owns two
   *  BabylonBuildupEffects (one for the target's scan ring + grid hologram,
   *  one for the builder's beam) and the interval that drives the buildup
   *  progress 0→1 loop. Volatile — never persisted; a saved scene loads idle. */
  private readonly buildEffects = new Map<number, {
    builderEffect: BabylonBuildupEffect;
    targetEffect: BabylonBuildupEffect;
    intervalHandle: ReturnType<typeof setInterval>;
    targetId: number;
  }>();
  private readonly buildTick = signal(0);
  /** Active factory fabrication effects keyed by factory item id. Each entry
   *  owns the scan plate, the cloned preview unit, the preview buildup effect,
   *  and the interval driving the progress loop. Volatile — never persisted. */
  private readonly fabricateEffects = new Map<number, {
    scanEffect: BabylonBuildupEffect;
    previewBuildupEffect: BabylonBuildupEffect;
    previewRenderObject: RenderObject;
    intervalHandle: ReturnType<typeof setInterval>;
    /** Set to true after the factory's intro animation finishes — the progress
     *  loop only ticks while this is true so the columns finish lowering before
     *  we start scrubbing them back up. */
    inProgressPhase: boolean;
    progressStartTime: number;
  }>();
  private readonly fabricateTick = signal(0);

  async ngOnInit(): Promise<void> {
    if (!this.hasToken()) return;
    await this.reload();
    // Library content lives in ThumbnailStorageService — load once so the
    // library list is populated when the user opens any scene.
    if (this.thumbStorage.items().length === 0) {
      await this.thumbStorage.loadAll().catch(() => {});
    }
    // Planet list for the terrain dropdown.
    try {
      this.planets.set(await this.terrainLoader.listPlanets());
    } catch (e) {
      console.warn('[Studio] planet list failed', e);
    }
  }

  ngAfterViewInit(): void {
    // Canvas is in DOM (hidden via [hidden]); engine bootstraps on first
    // openScene so users who never visit /scenes pay no Babylon cost.
  }

  hasToken(): boolean { return !!localStorage.getItem('app.token'); }
  itemKey(it: ThumbnailItem): string { return `${it.kind}:${it.id}`; }

  setGizmoTool(tool: GizmoTool): void {
    this.gizmoTool.set(tool);
    this.applyGizmoTool();
  }

  private applyGizmoTool(): void {
    if (!this.gizmoManager) return;
    const tool = this.gizmoTool();
    this.gizmoManager.positionGizmoEnabled = tool === 'move';
    this.gizmoManager.rotationGizmoEnabled = tool === 'rotate';
    this.gizmoManager.scaleGizmoEnabled = tool === 'scale';
    // Re-attach to current selection so the newly-enabled gizmo appears.
    this.attachGizmoToSelection();
  }

  /** Blender-ish shortcuts; ignore when typing into an input. */
  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    // Preview modal owns Escape while open — close it before any other handler
    // (gizmo/deselect) sees the key. Block other shortcuts while the modal is
    // up so 'g'/'r'/'s' don't silently flip the gizmo tool behind the preview.
    if (this.screenshotPreview()) {
      if (event.key === 'Escape') {
        this.discardScreenshot();
        event.preventDefault();
      }
      return;
    }
    if (!this.current() || !this.rendererReady()) return;
    const target = event.target as HTMLElement | null;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'SELECT' || target.tagName === 'TEXTAREA')) return;
    switch (event.key.toLowerCase()) {
      case 'g': this.setGizmoTool('move'); break;
      case 'r': this.setGizmoTool('rotate'); break;
      case 's': this.setGizmoTool('scale'); break;
      case 'h': this.setGizmoTool('none'); break;
      case 'escape': this.deselect(); break;
      default: return;
    }
    event.preventDefault();
  }

  /** Drop the current selection and hide the gizmo. */
  deselect(): void {
    this.selectedItemId.set(null);
    this.gizmoManager?.attachToMesh(null);
  }

  filteredLibrary(): ThumbnailItem[] {
    const f = this.libraryFilter.trim().toLowerCase();
    const all = this.thumbStorage.items();
    if (!f) return all;
    return all.filter(i => i.internalName.toLowerCase().includes(f));
  }

  selectedItem(): SceneItem | null {
    const sel = this.selectedItemId();
    if (sel == null) return null;
    return this.content()?.items.find(i => i.id === sel) ?? null;
  }

  libraryLabel(it: SceneItem): string {
    const lib = this.thumbStorage.items().find(l => l.kind === it.kind && l.id === it.itemTypeId);
    return lib?.internalName ?? `${it.kind}#${it.itemTypeId}`;
  }

  /** All scene items excluding the given id (used by the attack-target dropdown). */
  otherItems(excludeId: number): SceneItem[] {
    return (this.content()?.items ?? []).filter(i => i.id !== excludeId);
  }

  /** Resource items in the scene, excluding the given id. Used by the
   *  harvest-target dropdown — only resource-kind items make sense as
   *  the source of the BabylonHarvestingBeam's crystals. */
  harvestTargets(excludeId: number): SceneItem[] {
    return (this.content()?.items ?? []).filter(i => i.id !== excludeId && i.kind === 'resource');
  }

  /** Base items in the scene, excluding the given id. Used by the
   *  build-target dropdown — only base-kind items receive a BabylonBuildupEffect
   *  scan ring + grid hologram during construction. */
  buildTargets(excludeId: number): SceneItem[] {
    return (this.content()?.items ?? []).filter(i => i.id !== excludeId && i.kind === 'base');
  }

  /** All base-kind items in the thumbnail library that have a model3D — these
   *  are the unit types a factory can fabricate. */
  fabricateTypes(): ThumbnailItem[] {
    return this.thumbStorage.items().filter(i => i.kind === 'base' && i.model3DId != null);
  }

  // ===== Attack VFX =====

  isLooping(id: number): boolean {
    // Read loopTick so isLooping() re-evaluates when toggleAttackLoop runs.
    this.loopTick();
    return this.attackLoops.has(id);
  }

  toggleAttackLoop(id: number): void {
    if (this.attackLoops.has(id)) {
      this.stopAttackLoop(id);
    } else {
      // ~600ms keeps a couple lightning bolts alive at once and gives the user
      // a wide window for screenshotting; cheap enough not to hurt the loop.
      const handle = setInterval(() => this.fireAttack(id), 600);
      this.attackLoops.set(id, handle);
      this.fireAttack(id); // fire immediately so the user gets visual feedback
    }
    this.loopTick.update(n => n + 1);
  }

  private stopAttackLoop(id: number): void {
    const h = this.attackLoops.get(id);
    if (h != null) {
      clearInterval(h);
      this.attackLoops.delete(id);
      this.loopTick.update(n => n + 1);
    }
  }

  /**
   * Plays the production lightning VFX from attacker → target: muzzle flash at
   * the beam origin, a lightning bolt between the two, and an impact burst on
   * the target. Other weapon kinds (projectile/rocket) would need the trail +
   * flying mesh path from babylon-base-item, deferred for now.
   */
  fireAttack(attackerId: number): void {
    const attacker = this.content()?.items.find(i => i.id === attackerId);
    if (!attacker || attacker.attackTargetId == null) return;
    const target = this.content()?.items.find(i => i.id === attacker.attackTargetId);
    if (!target) return;

    const attackerRender = this.renderObjects.get(attackerId);
    const targetNode = this.nodes.get(target.id);
    if (!attackerRender || !targetNode) return;

    const scene = this.babylonRender.getScene();
    const start = attackerRender.getBeamOrigin() ?? attackerRender.getModel3D().getAbsolutePosition().clone();
    // Lift impact off the ground so the bolt terminates inside the target mesh
    // rather than at its pivot. Half the item scale is a good rough match.
    const end = targetNode.position.clone();
    end.y += Math.max(1, target.scale * 1.5);

    // Make sure the target is visible at the start of every cycle — the
    // previous fire may have hidden it via explode-on-fire, and looping callers
    // expect the target to "respawn" each tick.
    targetNode.setEnabled(true);

    BabylonMuzzleFlash.fire(scene, start, end.subtract(start).normalize());
    // Long bolt lifetime so the bolt is still visible at late capture delays
    // (e.g. 500ms when the explosion fireball is at its peak). Default 700ms
    // would leave the bolt at ~32% alpha at 500ms; 3000ms keeps it at ~85%.
    BabylonLightning.fire(scene, start, end, 3000);
    BabylonImpact.detonate(scene, end);
    if (attacker.explodeTargetOnFire) {
      // Brief delay so the explosion reads as a consequence of the bolt, not
      // simultaneous with the muzzle flash. Lightning lifetime is ~250ms in
      // BabylonLightning, so 180ms lands the explosion mid-bolt.
      setTimeout(() => this.explodeItem(target.id, /*hide*/ true), 180);
    }
  }

  /**
   * Fire the attack VFX and auto-trigger a screenshot at the configured delay,
   * so the user doesn't have to hit "Take screenshot" with frame-level timing
   * to catch the lightning bolt peak (~70ms) or the impact explosion (~180ms).
   */
  async fireAndCapture(attackerId: number): Promise<void> {
    this.fireAttack(attackerId);
    const delay = Math.max(0, this.captureDelayMs | 0);
    await new Promise(r => setTimeout(r, delay));
    await this.takeScreenshot();
  }

  // ===== Harvest VFX =====

  isHarvesting(id: number): boolean {
    // Read harvestTick so the template re-evaluates when toggleHarvest runs.
    this.harvestTick();
    return this.harvestingBeams.has(id);
  }

  /** Start or stop the harvesting beam for the given item. The beam is
   *  continuous (crystals spiral indefinitely) so a single toggle is enough —
   *  no Loop/Fire-once distinction like the attack VFX. */
  toggleHarvest(id: number): void {
    if (this.harvestingBeams.has(id)) {
      this.stopHarvest(id);
      return;
    }
    const item = this.content()?.items.find(i => i.id === id);
    if (!item || item.harvestTargetId == null) return;
    const target = this.content()?.items.find(i => i.id === item.harvestTargetId);
    if (!target) return;
    const renderObject = this.renderObjects.get(id);
    const targetNode = this.nodes.get(target.id);
    if (!renderObject || !targetNode) return;

    const scene = this.babylonRender.getScene();
    // getBeamOrigin / getContainerPosition are read every frame so the beam
    // tracks live movement if the user drags the harvester via the gizmo.
    const beam = new BabylonHarvestingBeam(
      scene,
      () => renderObject.getBeamOrigin() ?? renderObject.getModel3D().getAbsolutePosition().clone(),
      () => renderObject.getModel3D().getAbsolutePosition().clone(),
    );
    // Resource crystals spawn at the top of the rock cluster — lift the target
    // off the ground so the helix terminates inside the rock, not at its base.
    const targetPos = targetNode.position.clone();
    targetPos.y += Math.max(0.5, target.scale * 0.8);
    beam.start(targetPos);
    this.harvestingBeams.set(id, beam);
    this.harvestTick.update(n => n + 1);
  }

  private stopHarvest(id: number): void {
    const beam = this.harvestingBeams.get(id);
    if (!beam) return;
    beam.dispose();
    this.harvestingBeams.delete(id);
    this.harvestTick.update(n => n + 1);
  }

  // ===== Build VFX (builder → building under construction) =====

  isBuilding(id: number): boolean {
    // Read buildTick so the template re-evaluates when toggleBuild runs.
    this.buildTick();
    return this.buildEffects.has(id);
  }

  /** Start or stop the construction beam + buildup hologram for the given
   *  item. Mirrors the production setBuildingPosition() flow: target gets a
   *  scan ring + grid material on its upper meshes, builder fires a beam at
   *  the scan ring, builder's intro/loop AnimationGroups play.
   *
   *  The buildup progress cycles 0→1 over 10s indefinitely so the user has a
   *  wide window to screenshot at any point in the construction. */
  toggleBuild(id: number): void {
    if (this.buildEffects.has(id)) {
      this.stopBuild(id);
      return;
    }
    const item = this.content()?.items.find(i => i.id === id);
    if (!item || item.buildTargetId == null) return;
    const target = this.content()?.items.find(i => i.id === item.buildTargetId);
    if (!target) return;
    const renderObject = this.renderObjects.get(id);
    const builderNode = this.nodes.get(id);
    const targetNode = this.nodes.get(target.id);
    if (!renderObject || !builderNode || !targetNode) return;

    const scene = this.babylonRender.getScene();

    // Target effect: scan ring + grid material on the building's meshes.
    // BabylonBuildupEffect.init() captures the meshes' current materials so we
    // can restore them on stop; createRing() builds the holographic plate +
    // sparks under the building.
    const targetRadius = this.estimateNodeRadius(targetNode);
    const targetEffect = new BabylonBuildupEffect(
      scene, targetNode as unknown as Mesh,
      BabylonRenderServiceAccessImpl.color4Diplomacy(target.diplomacy as Diplomacy),
      targetRadius,
    );
    targetEffect.init();
    targetEffect.createRing();
    // Initial paint so the very first frame already shows grid material —
    // otherwise init() left meshes invisible until the first interval tick.
    targetEffect.updateVisibility(0);

    // Builder effect: only used to hold the construction beam, no scan ring.
    const builderEffect = new BabylonBuildupEffect(
      scene, builderNode as unknown as Mesh,
      BabylonRenderServiceAccessImpl.color4Diplomacy(item.diplomacy as Diplomacy),
      this.estimateNodeRadius(builderNode),
    );

    // The beam starts AFTER the builder's intro animation finishes (e.g. the
    // arm rises into position). For units without an intro phase the callback
    // fires synchronously.
    renderObject.setBuildAnimationActive(true, () => {
      // Defensive: stopBuild may have fired between toggleBuild and intro end.
      if (!this.buildEffects.has(id)) return;
      builderEffect.startBuildingBeam(
        targetNode.position.clone(),
        {
          getBeamOrigin: () => renderObject.getBeamOrigin(),
          getContainerPosition: () => renderObject.getModel3D().getAbsolutePosition().clone(),
        },
        (_target) => {
          // Resolve the beam endpoint each frame from the target effect's
          // scan-line absolute position — tracks live if the user drags
          // either item with the gizmo.
          if (targetEffect.scanLine) {
            targetEffect.scanLine.computeWorldMatrix(true);
            return targetEffect.scanLine.getAbsolutePosition().clone();
          }
          return null;
        }
      );
    });

    // Animate buildup 0→1 over 10s, loop. Looping (rather than holding at 1)
    // keeps grid material visible for screenshots regardless of when the user
    // hits Capture.
    const cycleMs = 10000;
    const startTime = Date.now();
    const intervalHandle = setInterval(() => {
      const elapsed = (Date.now() - startTime) % cycleMs;
      targetEffect.updateVisibility(elapsed / cycleMs);
    }, 50);

    this.buildEffects.set(id, {builderEffect, targetEffect, intervalHandle, targetId: target.id});
    this.buildTick.update(n => n + 1);
  }

  private stopBuild(id: number): void {
    const eff = this.buildEffects.get(id);
    if (!eff) return;
    clearInterval(eff.intervalHandle);
    eff.builderEffect.cleanup();
    eff.targetEffect.cleanup();
    this.renderObjects.get(id)?.setBuildAnimationActive(false);
    this.buildEffects.delete(id);
    this.buildTick.update(n => n + 1);
  }

  /** Half the larger of the local XZ bounding-box extents across all visible
   *  child meshes. Used as the BabylonBuildupEffect radius for items where we
   *  don't have access to the production PhysicalAreaConfig. */
  private estimateNodeRadius(node: TransformNode): number {
    node.computeWorldMatrix(true);
    let radius = 1;
    for (const child of node.getChildMeshes()) {
      if (!child.isVisible || child.getTotalVertices() === 0) continue;
      const bb = child.getBoundingInfo().boundingBox;
      const dx = bb.maximumWorld.x - bb.minimumWorld.x;
      const dz = bb.maximumWorld.z - bb.minimumWorld.z;
      radius = Math.max(radius, Math.max(dx, dz) / 2);
    }
    return radius;
  }

  // ===== Fabricate VFX (factory building a unit) =====

  isFabricating(id: number): boolean {
    // Read fabricateTick so the template re-evaluates when toggleFabricate runs.
    this.fabricateTick();
    return this.fabricateEffects.has(id);
  }

  /** Start or stop the factory's fabrication animation. Mirrors the production
   *  setConstructing() flow: factory plays intro (columns lower), then loops
   *  progress 0→1 (columns rise, scan plate tracks them, preview unit is
   *  assembled with grid material clipping above the scan plate).
   *
   *  Requires the factory's RenderObject to have progress AnimationGroups —
   *  no-op with a console warn otherwise. */
  toggleFabricate(id: number): void {
    if (this.fabricateEffects.has(id)) {
      this.stopFabricate(id);
      return;
    }
    const item = this.content()?.items.find(i => i.id === id);
    if (!item || item.fabricateTypeId == null) return;
    const renderObject = this.renderObjects.get(id);
    const factoryNode = this.nodes.get(id);
    if (!renderObject || !factoryNode) return;

    if (!renderObject.hasProgressAnimation()) {
      console.warn('[Studio] item', id, 'has no progress animation — not a factory model');
      return;
    }
    const footprint = renderObject.getProgressAnimationFootprintExtents();
    if (!footprint) {
      console.warn('[Studio] factory has no footprint extents');
      return;
    }
    const unitLib = this.thumbStorage.items().find(l => l.kind === 'base' && l.id === item.fabricateTypeId);
    if (!unitLib || unitLib.model3DId == null) {
      console.warn('[Studio] fabricate type', item.fabricateTypeId, 'has no model3D');
      return;
    }

    const scene = this.babylonRender.getScene();

    // Holographic build preview cloned inside the factory container so the
    // model rises with the column animation (the preview's mesh visibility is
    // clipped above the scan plate via the buildup effect).
    const previewRenderObject = this.babylonModel.cloneModel3D(unitLib.model3DId, factoryNode, item.diplomacy as Diplomacy);
    previewRenderObject.setPositionXZ(footprint.centerX, footprint.centerZ);

    // Rectangular scan plate sized to the factory's rising-column footprint.
    const scanEffect = new BabylonBuildupEffect(
      scene, factoryNode as unknown as Mesh,
      BabylonRenderServiceAccessImpl.color4Diplomacy(item.diplomacy as Diplomacy),
      Math.min(footprint.width, footprint.depth) / 2,
    );
    scanEffect.createRectangularScan(footprint.width, footprint.depth);
    if (scanEffect.scanLine) {
      scanEffect.scanLine.position.x = footprint.centerX;
      scanEffect.scanLine.position.z = footprint.centerZ;
    }

    // Preview buildup: drives the clip line so meshes below the scan plate use
    // the unit's normal material and meshes above use the grid hologram. We
    // skip createRing() — the factory has its own scan plate.
    const previewMesh = previewRenderObject.getModel3D() as Mesh;
    const previewBuildupEffect = new BabylonBuildupEffect(
      scene, previewMesh,
      BabylonRenderServiceAccessImpl.color4Diplomacy(item.diplomacy as Diplomacy),
      1, // unused: createRing is not called
    );
    previewBuildupEffect.init();

    const state = {
      scanEffect, previewBuildupEffect, previewRenderObject,
      intervalHandle: null as any,
      inProgressPhase: false,
      progressStartTime: 0,
    };

    renderObject.setBuildAnimationActive(true, () => {
      // Defensive: stopFabricate may have fired before the intro ended.
      if (!this.fabricateEffects.has(id)) return;
      state.inProgressPhase = true;
      state.progressStartTime = Date.now();
    });

    // Animate progress 0→1 over 8s, loop. Each tick scrubs the column
    // animation, repositions the scan plate, and updates the preview clip.
    const cycleMs = 8000;
    state.intervalHandle = setInterval(() => {
      if (!state.inProgressPhase) return;
      const elapsed = (Date.now() - state.progressStartTime) % cycleMs;
      const progress = elapsed / cycleMs;
      renderObject.setProgressAnimationValue(progress);
      const yRange = renderObject.getProgressAnimationLocalYRange();
      if (yRange) {
        const localY = yRange.min + progress * (yRange.max - yRange.min);
        if (state.scanEffect.scanLine) {
          state.scanEffect.scanLine.position.y = localY;
        }
        const worldScanY = factoryNode.position.y + localY;
        state.previewBuildupEffect.applyMeshVisibilityAtWorldY(worldScanY);
      }
    }, 33);

    this.fabricateEffects.set(id, state);
    this.fabricateTick.update(n => n + 1);
  }

  private stopFabricate(id: number): void {
    const eff = this.fabricateEffects.get(id);
    if (!eff) return;
    clearInterval(eff.intervalHandle);
    eff.scanEffect.cleanupRing();
    eff.previewBuildupEffect.cleanup();
    eff.previewRenderObject.dispose();
    this.renderObjects.get(id)?.setBuildAnimationActive(false);
    this.fabricateEffects.delete(id);
    this.fabricateTick.update(n => n + 1);
  }

  // ===== Resource VFX (decal + sparkle) =====

  /** Mirror the in-game BabylonResourceItemImpl visuals: ground decal under
   *  the rock and the floating blue-white sparkle particles around it. Uses
   *  item.scale as the radius proxy since the studio doesn't load the actual
   *  ResourceItemType.getRadius() value. */
  private spawnResourceVfx(item: SceneItem): void {
    if (item.kind !== 'resource') return;
    const scene = this.babylonRender.getScene();
    const radius = item.scale;
    const sparkle = new BabylonResourceSparkle(scene, radius);
    sparkle.emitter.copyFrom(new Vector3(...item.position));
    const decal = new BabylonResourceDecal(scene, radius, this.babylonRender);
    decal.updatePosition(item.position[0], item.position[1], item.position[2]);
    this.resourceSparkles.set(item.id, sparkle);
    this.resourceDecals.set(item.id, decal);
    this.resourceRadii.set(item.id, radius);
  }

  private updateResourceVfx(item: SceneItem): void {
    if (item.kind !== 'resource') return;
    // Radius is baked into both the sparkle's emit box and the decal's size at
    // construction. If scale changed we have to rebuild — otherwise we'd see
    // the particle box detach from the rock and the decal stay at old size.
    const lastRadius = this.resourceRadii.get(item.id);
    if (lastRadius !== item.scale) {
      this.disposeResourceVfx(item.id);
      this.spawnResourceVfx(item);
      return;
    }
    // Position-only: in-place updates so the existing particles keep flowing
    // (no visible restart while the user drags the gizmo).
    this.resourceSparkles.get(item.id)?.emitter.copyFrom(new Vector3(...item.position));
    this.resourceDecals.get(item.id)?.updatePosition(item.position[0], item.position[1], item.position[2]);
  }

  private disposeResourceVfx(id: number): void {
    this.resourceSparkles.get(id)?.dispose();
    this.resourceDecals.get(id)?.dispose();
    this.resourceSparkles.delete(id);
    this.resourceDecals.delete(id);
    this.resourceRadii.delete(id);
  }

  /**
   * Detonate an item's position with the production explosion VFX (fireball +
   * debris + shockwave). When hide is true, the item's model is removed once
   * the fireball is in bloom — so the result reads as "destroyed" rather than
   * "fireball next to an undamaged Viper".
   */
  explodeItem(id: number, hide = false): void {
    const node = this.nodes.get(id);
    if (!node) return;
    const item = this.content()?.items.find(i => i.id === id);
    // Lift the detonation off the ground so the fireball engulfs the body
    // instead of igniting at the feet.
    const center = node.position.clone();
    center.y += Math.max(1, (item?.scale ?? 1) * 1.5);
    BabylonExplosion.detonate(this.babylonRender.getScene(), center);
    if (hide) {
      // 120ms into the explosion the fireball is opaque enough to mask the
      // model disappearing — earlier looks like a jump-cut, later spoils it.
      setTimeout(() => node.setEnabled(false), 120);
      // Scorch + lingering smoke at the ground footprint. Delayed until the
      // fireball is in its fade phase (~600ms into the 1.0–1.4s lifetime),
      // otherwise the dark ground decal competes with the bright fireball
      // and the PNG reads as "wreckage in front of explosion".
      // Short lifetime+fade (vs the in-game 15s+5s) so back-to-back screenshot
      // sessions aren't gated on stale wreckage from the previous capture.
      setTimeout(() => {
        const radius = Math.max(1, (item?.scale ?? 1) * 2);
        BabylonWreckage.spawn(
          this.babylonRender.getScene(), node.position.clone(), radius,
          undefined, false, {lifetimeMs: 1000, fadeMs: 1000},
        );
      }, 600);
      // Respawn after 5s so the user keeps a continuously-readable scene
      // (otherwise an "Explode now" leaves a hole on screen until reload).
      // No-op in loop mode — fireAttack re-enables at the start of every
      // cycle anyway.
      setTimeout(() => node.setEnabled(true), 5000);
    }
  }

  /** Rotate the attacker's turret (if any) to face its current target. */
  private aimTurret(attackerId: number): void {
    const attacker = this.content()?.items.find(i => i.id === attackerId);
    if (!attacker) return;
    const renderObject = this.renderObjects.get(attackerId);
    const turret = renderObject?.getTurretMesh?.();
    if (!turret) return;
    if (attacker.attackTargetId == null) {
      turret.rotation.y = 0;
      return;
    }
    const target = this.content()?.items.find(i => i.id === attacker.attackTargetId);
    const targetNode = target ? this.nodes.get(target.id) : null;
    if (!targetNode) return;
    const dx = targetNode.position.x - this.nodes.get(attackerId)!.position.x;
    const dz = targetNode.position.z - this.nodes.get(attackerId)!.position.z;
    turret.rotation.y = Math.atan2(dx, dz) - attacker.rotationY;
  }

  // ===== Scene CRUD =====

  async reload(): Promise<void> {
    this.loading.set(true);
    try { await this.storage.list(); } finally { this.loading.set(false); }
  }

  async createScene(): Promise<void> {
    this.busy.set(true);
    try {
      const name = `Scene ${this.storage.scenes().length + 1}`;
      const summary = await this.storage.create(name, emptyScene());
      await this.openScene(summary.id);
    } finally {
      this.busy.set(false);
    }
  }

  async openScene(id: number): Promise<void> {
    this.busy.set(true);
    this.rendererStatus.set(this.rendererReady() ? 'Loading scene…' : 'Initialising renderer…');
    try {
      const {summary, content} = await this.storage.read(id);
      this.disposeAllItems();
      this.terrainLoader.disposeTerrain();
      this.current.set(summary);
      this.content.set(content);
      this.currentName = summary.name;
      this.selectedItemId.set(null);

      // Wait one frame so the canvas (just un-hidden via *if/hidden) has
      // its layout size before Babylon binds the WebGL viewport.
      await new Promise(r => requestAnimationFrame(r));
      await this.ensureRenderer();
      this.babylonRender.getScene().getEngine().resize();

      // Apply the saved camera BEFORE terrain so regionToLoad() clips around
      // the real target (otherwise the camera is still at its default origin
      // and we'd load the wrong corner of the map).
      this.applyCamera(content);

      // Recompute next id from loaded content so re-saves don't clash.
      this.nextItemId = content.items.reduce((m, i) => Math.max(m, i.id), 0) + 1;
      for (const item of content.items) {
        this.spawnNode(item);
      }
      // After every node exists, aim turrets so any persisted attackTargetId
      // shows up visually even before the user clicks anything.
      for (const item of content.items) {
        if (item.attackTargetId != null) this.aimTurret(item.id);
      }
      // Terrain — fired after items so terrain build progress isn't blocking
      // the item display. Clipped to a region (see regionToLoad) so we don't
      // build the whole planet. Errors are logged but don't abort scene open.
      if (content.terrain?.planetId) {
        this.rendererStatus.set('Loading terrain…');
        await this.terrainLoader.loadTerrain(content.terrain.planetId, this.regionToLoad()).catch(e => {
          console.warn('[Studio] terrain load failed', e);
        });
      }
      this.rendererStatus.set('');
    } catch (e) {
      console.warn('[Studio] scene open failed', e);
      this.rendererStatus.set('');
    } finally {
      this.busy.set(false);
    }
  }

  async saveScene(): Promise<void> {
    const cur = this.current();
    const cnt = this.content();
    if (!cur || !cnt) return;
    this.busy.set(true);
    try {
      // Flush scene-node transforms into content.items (gizmo edits live on
      // the nodes, not in the model until we save).
      for (const item of cnt.items) {
        const node = this.nodes.get(item.id);
        if (!node) continue;
        item.position = [node.position.x, node.position.y, node.position.z];
        item.rotationY = node.rotation.y;
        item.scale = node.scaling.x;
      }
      // Camera state.
      if (this.camera) {
        cnt.camera = {
          mode: 'arc',
          position: [this.camera.position.x, this.camera.position.y, this.camera.position.z],
          target: [this.camera.target.x, this.camera.target.y, this.camera.target.z],
          alpha: this.camera.alpha, beta: this.camera.beta, radius: this.camera.radius
        };
      }
      const updated = await this.storage.save(cur.id, this.currentName, cnt);
      this.current.set(updated);
    } catch (e) {
      console.warn('[Studio] scene save failed', e);
    } finally {
      this.busy.set(false);
    }
  }

  async deleteScene(s: StudioSceneSummary, event: Event): Promise<void> {
    event.stopPropagation();
    if (!confirm(`Delete scene "${s.name}"? This cannot be undone.`)) return;
    this.busy.set(true);
    try {
      await this.storage.delete(s.id);
      if (this.current()?.id === s.id) {
        this.disposeAllItems();
        this.terrainLoader.disposeTerrain();
        this.current.set(null);
        this.content.set(null);
        this.currentName = '';
        this.selectedItemId.set(null);
      }
    } finally {
      this.busy.set(false);
    }
  }

  // ===== Item placement =====

  spawnFromLibrary(libItem: ThumbnailItem): void {
    if (libItem.model3DId == null) return;
    // Spawn at what the camera is looking at, so the new item lands in the
    // viewport's centre instead of always at world origin. Snap Y to the
    // terrain height under that XZ so units don't float / sink into the
    // ground — useful when populating a background-shot with many items
    // without manually fixing every Y in the gizmo afterwards.
    const t = this.camera?.target ?? Vector3.Zero();
    const groundY = this.pickTerrainHeight(t.x, t.z);
    const item: SceneItem = {
      id: this.nextItemId++,
      kind: libItem.kind,
      itemTypeId: libItem.id,
      position: [t.x, groundY ?? t.y, t.z],
      rotationY: 0,
      scale: 1,
      diplomacy: 'OWN'
    };
    this.content.update(c => c ? {...c, items: [...c.items, item]} : c);
    this.spawnNode(item);
    this.selectedItemId.set(item.id);
    this.attachGizmoToSelection();
  }

  /** Raycast straight down through the loaded terrain meshes at (x, z) and
   *  return the Y of the first hit, or null if there's no terrain there
   *  (scene without a planet, or the XZ is outside the loaded tiles). Mirrors
   *  the picker BabylonResourceDecal uses for its decal placement. */
  private pickTerrainHeight(x: number, z: number): number | null {
    if (!this.rendererReady()) return null;
    const scene = this.babylonRender.getScene();
    const ray = new Ray(new Vector3(x, 1000, z), new Vector3(0, -1, 0), 2000);
    const pick = scene.pickWithRay(ray, (mesh: AbstractMesh) => {
      const meta = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
      if (!meta) return false;
      return meta.type === RazarionMetadataType.GROUND || meta.type === RazarionMetadataType.BOT_GROUND;
    });
    return pick?.pickedPoint?.y ?? null;
  }

  /** Persist + reload terrain when the user picks a different planet. */
  async onPlanetChange(event: Event): Promise<void> {
    const raw = (event.target as HTMLSelectElement).value;
    const planetId = raw ? parseInt(raw, 10) : null;
    this.content.update(c => {
      if (!c) return c;
      const terrain: SceneTerrain | null = planetId ? {planetId, region: null} : null;
      return {...c, terrain};
    });
    this.rendererStatus.set('Loading terrain…');
    try {
      await this.terrainLoader.loadTerrain(planetId, this.regionToLoad());
    } catch (e) {
      console.warn('[Studio] terrain load failed', e);
    } finally {
      this.rendererStatus.set('');
    }
  }

  // ===== Terrain region (clip the planet so the studio stays fast) =====

  /** The {x, y, w, h} box (game-plane world units) to clip the terrain build
   *  to. Explicit scene region wins; otherwise a default-sized box centred on
   *  the current camera target. null only when "Full map" is selected. */
  private regionToLoad(): {x: number; y: number; w: number; h: number} | null {
    if (this.loadFullMap()) return null;
    const region = this.content()?.terrain?.region;
    if (region) return region;
    const t = this.camera?.target ?? Vector3.Zero();
    const s = SceneComposerTaskComponent.DEFAULT_REGION_SIZE;
    return {x: t.x - s / 2, y: t.z - s / 2, w: s, h: s};
  }

  /** Re-build the terrain with the current region settings. */
  async reloadTerrain(): Promise<void> {
    const planetId = this.content()?.terrain?.planetId;
    if (!planetId) {
      this.terrainLoader.disposeTerrain();
      return;
    }
    this.rendererStatus.set('Loading terrain…');
    try {
      await this.terrainLoader.loadTerrain(planetId, this.regionToLoad());
    } catch (e) {
      console.warn('[Studio] terrain load failed', e);
    } finally {
      this.rendererStatus.set('');
    }
  }

  regionMode(): 'auto' | 'custom' | 'full' {
    if (this.loadFullMap()) return 'full';
    return this.content()?.terrain?.region ? 'custom' : 'auto';
  }

  onRegionModeChange(event: Event): void {
    const mode = (event.target as HTMLSelectElement).value as 'auto' | 'custom' | 'full';
    if (mode === 'full') {
      this.loadFullMap.set(true);
      this.content.update(c => (c?.terrain ? {...c, terrain: {...c.terrain, region: null}} : c));
    } else if (mode === 'auto') {
      this.loadFullMap.set(false);
      this.content.update(c => (c?.terrain ? {...c, terrain: {...c.terrain, region: null}} : c));
    } else {
      // custom: seed an explicit box from the current default so the inputs
      // start populated, then let the user edit + Apply.
      this.loadFullMap.set(false);
      const seed = this.regionToLoad() ?? {x: 0, y: 0, w: SceneComposerTaskComponent.DEFAULT_REGION_SIZE, h: SceneComposerTaskComponent.DEFAULT_REGION_SIZE};
      this.content.update(c => (c?.terrain ? {...c, terrain: {...c.terrain, region: {...seed}}} : c));
    }
    this.reloadTerrain();
  }

  /** Edit one field of the custom region (no reload — user hits Apply). */
  updateRegion(field: 'x' | 'y' | 'w' | 'h', value: number): void {
    this.content.update(c => {
      if (!c?.terrain?.region) return c;
      const region = {...c.terrain.region, [field]: +value};
      return {...c, terrain: {...c.terrain, region}};
    });
  }

  /** Recentre the custom region on the current camera target (keeps w/h). */
  centerRegionOnCamera(): void {
    const t = this.camera?.target ?? Vector3.Zero();
    this.content.update(c => {
      if (!c?.terrain) return c;
      const cur = c.terrain.region;
      const w = cur?.w ?? SceneComposerTaskComponent.DEFAULT_REGION_SIZE;
      const h = cur?.h ?? SceneComposerTaskComponent.DEFAULT_REGION_SIZE;
      return {...c, terrain: {...c.terrain, region: {x: t.x - w / 2, y: t.z - h / 2, w, h}}};
    });
    this.reloadTerrain();
  }

  deleteSelected(): void {
    const id = this.selectedItemId();
    if (id == null) return;
    this.disposeItem(id);
    this.content.update(c => c ? {...c, items: c.items.filter(i => i.id !== id)} : c);
    this.selectedItemId.set(null);
    this.gizmoManager?.attachToMesh(null);
  }

  updateProp(field: 'px' | 'py' | 'pz' | 'ry' | 's' | 'diplomacy' | 'attackTargetId' | 'explodeTargetOnFire' | 'harvestTargetId' | 'buildTargetId' | 'fabricateTypeId', value: number | string | boolean): void {
    const id = this.selectedItemId();
    if (id == null) return;
    this.content.update(c => {
      if (!c) return c;
      const items = c.items.map(i => {
        if (i.id !== id) return i;
        const next = {...i};
        switch (field) {
          case 'px': next.position = [+value, i.position[1], i.position[2]]; break;
          case 'py': next.position = [i.position[0], +value, i.position[2]]; break;
          case 'pz': next.position = [i.position[0], i.position[1], +value]; break;
          case 'ry': next.rotationY = +value; break;
          case 's':  next.scale = Math.max(0.01, +value); break;
          case 'diplomacy': next.diplomacy = String(value); break;
          case 'attackTargetId': {
            const v = String(value);
            next.attackTargetId = v === '' ? null : parseInt(v, 10);
            // Stop a running loop if the target was cleared.
            if (next.attackTargetId == null) this.stopAttackLoop(id);
            break;
          }
          case 'explodeTargetOnFire': next.explodeTargetOnFire = Boolean(value); break;
          case 'harvestTargetId': {
            const v = String(value);
            next.harvestTargetId = v === '' ? null : parseInt(v, 10);
            // Stop the beam if the target was cleared or changed — the new
            // target (if any) requires a fresh toggle so the user sees the
            // change deliberately, not silently rebuilt against a new rock.
            this.stopHarvest(id);
            break;
          }
          case 'buildTargetId': {
            const v = String(value);
            next.buildTargetId = v === '' ? null : parseInt(v, 10);
            // Same idea as harvest: a target swap requires a fresh toggle so
            // the previous target's grid materials get restored cleanly.
            this.stopBuild(id);
            break;
          }
          case 'fabricateTypeId': {
            const v = String(value);
            next.fabricateTypeId = v === '' ? null : parseInt(v, 10);
            this.stopFabricate(id);
            break;
          }
        }
        return next;
      });
      return {...c, items};
    });
    this.applyToNode(id);
    if (field === 'attackTargetId') this.aimTurret(id);
  }

  // ===== Babylon plumbing =====

  private async ensureRenderer(): Promise<void> {
    if (this.rendererReady()) return;
    this.babylonRender.setup(this.canvasRef.nativeElement);
    const scene = this.babylonRender.getScene();
    scene.clearColor = new Color4(0.05, 0.06, 0.08, 1);

    // Production lighting parity — env IBL (added by `setup()` via
    // scene.createDefaultEnvironment) plus a directional light. A hemispheric
    // fill would compound with both and wash out the terrain ground.
    // babylon-terrain-tile.impl also reads directionalLight.includedOnlyMeshes
    // and shadowGenerator off the renderer service directly, so they must
    // exist before terrain build runs.
    const dirLight = new DirectionalLight('SceneDirectional', new Vector3(-3, -10, 3), scene);
    dirLight.intensity = 0.8;
    dirLight.shadowMinZ = -100;
    dirLight.shadowMaxZ = 200;
    dirLight.autoUpdateExtends = false;
    dirLight.shadowFrustumSize = 150;
    dirLight.diffuse = new Color3(1, 1, 1);
    dirLight.specular = new Color3(1, 1, 1);
    dirLight.shadowEnabled = true;
    this.babylonRender.directionalLight = dirLight;
    // 1024 (not 2048): the shadow map is re-rendered every frame for all
    // shadow casters, so it scales with terrain size. 1024 halves that cost
    // with no visible quality loss at studio framing distances.
    const shadowGenerator = new ShadowGenerator(1024, dirLight);
    shadowGenerator.useExponentialShadowMap = true;
    shadowGenerator.darkness = 0.6;
    this.babylonRender.shadowGenerator = shadowGenerator;

    // Pre-fire hidden warmup VFX so the FIRST visible bolt + impact don't
    // render blank while Babylon compiles shaders and uploads textures on
    // demand. Without this, "Fire + capture" misses the bolt on the very
    // first try because CreateScreenshotUsingRenderTarget's _RetryWithInterval
    // is still waiting for the bolt material to become ready when the bolt
    // has already faded.
    BabylonLightning.preWarm(scene);
    BabylonImpact.preWarm(scene);

    this.rendererStatus.set('Loading model library…');
    await this.babylonModel.init();

    this.camera = new ArcRotateCamera('scene-cam', -Math.PI / 4, Math.PI / 3, 20, Vector3.Zero(), scene);
    this.camera.wheelDeltaPercentage = 0.01;
    this.camera.pinchDeltaPercentage = 0.01;
    this.camera.lowerRadiusLimit = 0.5;
    this.camera.upperRadiusLimit = 5000;
    // Defaults are tuned for small models; a 5km terrain needs a far snappier
    // pan (lower = faster) and the inertia removed so dragging feels direct.
    this.camera.panningSensibility = 20;
    this.camera.panningInertia = 0;
    this.camera.inertia = 0.5;
    // WASD pans the camera target across the ground plane. Arrow keys keep
    // their default of rotating alpha/beta.
    this.camera.keysUp = [];
    this.camera.keysDown = [];
    this.camera.keysLeft = [];
    this.camera.keysRight = [];
    this.attachWasdPan(scene);
    this.camera.attachControl(this.canvasRef.nativeElement, true);
    scene.activeCamera = this.camera;

    this.gizmoManager = new GizmoManager(scene);
    this.gizmoManager.usePointerToAttachGizmos = false;
    this.applyGizmoTool();

    // Pointer-pick: walk up from the picked mesh to find its scene-item root.
    scene.onPointerObservable.add(info => {
      if (info.type !== PointerEventTypes.POINTERPICK) return;
      const picked = info.pickInfo?.pickedMesh;
      if (!picked) {
        this.selectedItemId.set(null);
        this.gizmoManager?.attachToMesh(null);
        return;
      }
      const sceneItemId = this.findOwningItemId(picked);
      if (sceneItemId != null) {
        this.selectedItemId.set(sceneItemId);
        this.attachGizmoToSelection();
      }
    });

    scene.getEngine().runRenderLoop(() => scene.render());
    this.rendererReady.set(true);
    this.rendererStatus.set('');
  }

  /**
   * Pan the camera target across the ground plane with WASD. Speed scales with
   * radius so it feels consistent whether you're zoomed close or looking at
   * the whole map. Q/E nudges the target up/down for terrain with vertical
   * features.
   */
  private attachWasdPan(scene: Scene): void {
    const held = new Set<string>();
    const canvas = this.canvasRef.nativeElement;
    canvas.tabIndex = 0;
    canvas.addEventListener('keydown', e => {
      const key = e.key.toLowerCase();
      if ('wasdqe'.includes(key)) {
        held.add(key);
        e.preventDefault();
      }
    });
    canvas.addEventListener('keyup', e => held.delete(e.key.toLowerCase()));
    canvas.addEventListener('blur', () => held.clear());
    scene.onBeforeRenderObservable.add(() => {
      if (!this.camera || held.size === 0) return;
      // dt in seconds, capped to avoid jumps when the tab was inactive.
      const dt = Math.min(0.05, scene.getEngine().getDeltaTime() / 1000);
      const speed = Math.max(2, this.camera.radius) * 1.5 * dt;
      // Forward = projection of camera→target onto the ground plane.
      const forward = this.camera.target.subtract(this.camera.position);
      forward.y = 0;
      forward.normalize();
      const right = new Vector3(forward.z, 0, -forward.x);
      const delta = new Vector3(0, 0, 0);
      if (held.has('w')) delta.addInPlace(forward.scale(speed));
      if (held.has('s')) delta.addInPlace(forward.scale(-speed));
      if (held.has('d')) delta.addInPlace(right.scale(speed));
      if (held.has('a')) delta.addInPlace(right.scale(-speed));
      if (held.has('e')) delta.y += speed;
      if (held.has('q')) delta.y -= speed;
      this.camera.target.addInPlace(delta);
    });
  }

  /**
   * Render the current view to a PNG at the chosen resolution and open it in
   * the preview modal — the user decides whether to download it via
   * saveScreenshot() or throw it away via discardScreenshot(). Uses
   * CreateScreenshotUsingRenderTarget so the output size is decoupled from the
   * canvas; for transparent backgrounds we briefly swap scene.clearColor to
   * (0,0,0,0) and restore afterwards.
   */
  async takeScreenshot(): Promise<void> {
    if (!this.camera || !this.rendererReady() || this.screenshotBusy()) return;
    const engine = this.babylonRender.getScene().getEngine();
    const scene = this.babylonRender.getScene();

    const preset = this.screenshotPreset;
    const width = preset.w > 0 ? preset.w : Math.max(64, Math.round(this.customWidth));
    const height = preset.h > 0 ? preset.h : Math.max(64, Math.round(this.customHeight));

    const originalClear = scene.clearColor.clone();
    if (this.screenshotBackground === 'transparent') {
      scene.clearColor = new Color4(0, 0, 0, 0);
    }
    this.screenshotBusy.set(true);
    try {
      const dataUrl = await new Promise<string>((resolve, reject) => {
        try {
          // samples=1, antialiasing=false: FXAA + MSAA each add an async
          // ready-check stage (FxaaPostProcess onCompiled) that adds frames
          // before the capture runs. For short-lived transient VFX like the
          // Tesla bolt (700ms) those extra frames let the bolt fade.
          //
          // customizeTexture: Babylon snapshots scene.meshes into renderList at
          // call time (screenshotTools.js: texture.renderList = scene.meshes.slice()).
          // _RetryWithInterval then waits for camera.isReady(true), which can
          // span several frames. During those frames the VFX's beforeRender keeps
          // ticking and may dispose the bolt/shockwave (lifetime 600-700ms).
          // The snapshot then points at disposed meshes → invisible in the PNG.
          // Setting renderList=null forces the RTT to use scene.meshes live at
          // render time, so whatever VFX exists when capture finally runs gets
          // rendered.
          // renderSprites: true — terrain tiles place SpriteManager-managed
          // grass / sand / beach decorations. RTT defaults to false and would
          // produce a screenshot of bare ground without any of these details.
          Tools.CreateScreenshotUsingRenderTarget(
            engine, this.camera!, {width, height}, resolve, 'image/png', 1, false,
            undefined, true, false, true, undefined,
            (texture) => { texture.renderList = null; }
          );
        } catch (e) { reject(e); }
      });
      this.screenshotPreview.set({
        dataUrl,
        w: width,
        h: height,
        filename: this.makeScreenshotFilename(width, height),
      });
    } catch (e) {
      console.error('[Studio] screenshot failed', e);
    } finally {
      scene.clearColor = originalClear;
      this.screenshotBusy.set(false);
    }
  }

  /** Download the pending preview as a PNG and close the dialog. */
  saveScreenshot(): void {
    const p = this.screenshotPreview();
    if (!p) return;
    const a = document.createElement('a');
    a.href = p.dataUrl;
    a.download = p.filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    this.screenshotPreview.set(null);
  }

  /** Throw away the pending preview without downloading. */
  discardScreenshot(): void {
    this.screenshotPreview.set(null);
  }

  private makeScreenshotFilename(w: number, h: number): string {
    const slug = (this.current()?.name ?? 'scene').replace(/[^a-z0-9-_]+/gi, '_').toLowerCase();
    const ts = new Date().toISOString().replace(/[:.]/g, '-').replace('T', '_').slice(0, 19);
    return `${slug}_${w}x${h}_${ts}.png`;
  }

  private spawnNode(item: SceneItem): void {
    const lib = this.thumbStorage.items().find(l => l.kind === item.kind && l.id === item.itemTypeId);
    if (!lib || lib.model3DId == null) {
      console.warn('[Studio] no library entry for', item);
      return;
    }
    const renderObject = this.babylonModel.cloneModel3D(lib.model3DId, null, item.diplomacy as Diplomacy);
    const node = renderObject.getModel3D();
    node.position = new Vector3(...item.position);
    node.rotation = new Vector3(0, item.rotationY, 0);
    node.scaling = new Vector3(item.scale, item.scale, item.scale);
    // Tag every descendant mesh so pointer-pick can map back to the item id.
    this.tagNodeTree(node, item.id);
    this.nodes.set(item.id, node);
    this.renderObjects.set(item.id, renderObject);
    this.spawnResourceVfx(item);
  }

  private tagNodeTree(node: TransformNode, sceneItemId: number): void {
    (node as any).sceneItemId = sceneItemId;
    for (const child of node.getChildren()) {
      if (child instanceof TransformNode) {
        this.tagNodeTree(child, sceneItemId);
      } else {
        (child as any).sceneItemId = sceneItemId;
      }
    }
  }

  private findOwningItemId(mesh: any): number | null {
    let node: any = mesh;
    while (node) {
      if (typeof node.sceneItemId === 'number') return node.sceneItemId;
      node = node.parent;
    }
    return null;
  }

  private applyToNode(id: number): void {
    const node = this.nodes.get(id);
    const item = this.content()?.items.find(i => i.id === id);
    if (!node || !item) return;
    node.position = new Vector3(...item.position);
    node.rotation = new Vector3(0, item.rotationY, 0);
    node.scaling = new Vector3(item.scale, item.scale, item.scale);
    // Diplomacy means re-cloning since material is baked at clone time —
    // detect and re-spawn if changed.
    const renderObject = this.renderObjects.get(id);
    if (renderObject) {
      const lib = this.thumbStorage.items().find(l => l.kind === item.kind && l.id === item.itemTypeId);
      const wasDiplomacy = (node as any).bakedDiplomacy ?? 'OWN';
      if (lib && lib.model3DId != null && wasDiplomacy !== item.diplomacy) {
        this.disposeItem(id);
        this.spawnNode(item);
        if (this.selectedItemId() === id) this.attachGizmoToSelection();
      } else {
        (node as any).bakedDiplomacy = item.diplomacy;
      }
    }
    // Re-aim this item's turret in case the move changed the bearing, and any
    // other item that's targeting this one — moving the target affects the
    // attacker's turret too.
    this.aimTurret(id);
    for (const other of this.content()?.items ?? []) {
      if (other.id !== id && other.attackTargetId === id) this.aimTurret(other.id);
    }
    this.updateResourceVfx(item);
  }

  private attachGizmoToSelection(): void {
    const id = this.selectedItemId();
    if (id == null || !this.gizmoManager) return;
    const node = this.nodes.get(id);
    if (!node) return;
    this.gizmoManager.attachToMesh(node as unknown as Mesh);
  }

  private applyCamera(content: SceneContent): void {
    if (!this.camera) return;
    const c = content.camera;
    // Target MUST be set first: ArcRotateCamera.target's setter internally
    // calls rebuildAnglesAndRadius(), which derives alpha/beta/radius from
    // the current world position relative to the new target — that would
    // overwrite our explicit saved values if we set them before the target.
    this.camera.target = new Vector3(...c.target);
    if (c.alpha != null) this.camera.alpha = c.alpha;
    if (c.beta != null) this.camera.beta = c.beta;
    if (c.radius != null) this.camera.radius = c.radius;
    // The next render loop tick reads target+alpha+beta+radius and computes
    // the new position automatically — no manual position recomputation
    // needed (and any setPosition/rebuildAnglesAndRadius call here would
    // overwrite the spherical values we just set).
  }

  private disposeAllItems(): void {
    for (const id of [...this.nodes.keys()]) this.disposeItem(id);
    this.gizmoManager?.attachToMesh(null);
  }

  private disposeItem(id: number): void {
    this.stopAttackLoop(id);
    this.stopHarvest(id);
    this.stopBuild(id);
    this.stopFabricate(id);
    // If this item was someone else's build target, that builder's targetEffect
    // is about to reference disposed meshes — stop those builds first so their
    // cleanup() runs while the meshes are still alive.
    for (const [builderId, eff] of this.buildEffects) {
      if (eff.targetId === id) this.stopBuild(builderId);
    }
    this.disposeResourceVfx(id);
    const ro = this.renderObjects.get(id);
    if (ro) ro.dispose();
    this.nodes.delete(id);
    this.renderObjects.delete(id);
  }
}
