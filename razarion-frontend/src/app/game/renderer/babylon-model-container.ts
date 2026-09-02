import {
  BabylonMaterialControllerClient,
  BabylonMaterialEntity,
  BaseEntity,
  GltfEntity,
  ParticleSystemControllerClient,
  ParticleSystemEntity
} from "../../generated/razarion-share";
import {
  AssetContainer,
  Material,
  NodeMaterial,
  NodeParticleSystemSet,
  PBRMaterial,
  Scene,
  SceneLoader
} from "@babylonjs/core";
import {TypescriptGenerator} from "../../backend/typescript-generator";
import {HttpClient} from "@angular/common/http";
import {BabylonModelService} from "./babylon-model.service";
import {URL_GLTF} from "../../common";
import {GltfHelper} from "./gltf-helper";
import {Diplomacy} from "../../gwtangular/GwtAngularFacade";
import {ISceneLoaderProgressEvent} from '@babylonjs/core/Loading/sceneLoader';
import {NgZone} from '@angular/core';

export abstract class BabylonModelContainer<E extends BaseEntity, B> {
  private entities: Map<number, E> = new Map();
  private loaded = false;
  private babylonModels: Map<number, B> = new Map();
  protected babylonModelService!: BabylonModelService;
  private loadingCount = 0;
  // Boot-freeze fix: models used to load all at once, so dozens of glTF parses piled up on the
  // main thread. We now queue them and keep at most maxConcurrentLoads() in flight, pumping the
  // next one whenever a load finishes.
  private pending: { entity: E; scene: Scene }[] = [];
  /**
   * Callers waiting for one specific entity, because the game no longer blocks on the whole set.
   * The boolean says whether the model actually arrived: a failed load has to wake its waiters
   * too, or an item whose glb 404s would sit invisible forever instead of falling back.
   */
  private waiting: Map<number, ((loaded: boolean) => void)[]> = new Map();
  /**
   * The entities the start gate waits for. Null means all of them, which is what this class did
   * before there was a reason to distinguish.
   * <p>
   * There is one: STARTUP_PAYLOAD measured a first visit and found 15 MB arriving before the game
   * was playable, of which 9.5 MB was this gate - every material and every particle system, in
   * full, while the glb models beside them had streamed individually for months. The single
   * largest item was a 4.8 MB vehicle material, waited for by a player who has not placed a base
   * yet and owns no vehicles.
   * <p>
   * A required entity is loaded first and holds the gate; the rest still load, just without
   * anybody standing in the doorway for them.
   */
  private required: Set<number> | null = null;
  private requiredOutstanding = 0;

  /** Max models parsed concurrently. Kept low for heavy main-thread parsing (e.g. glTF). */
  protected maxConcurrentLoads(): number {
    return 4;
  }

  /**
   * Which entities the start gate waits for. Call before {@link load}; an id that is not in the
   * set being loaded is ignored rather than deadlocking the gate.
   */
  setRequired(requiredIds: number[] | null): void {
    this.required = requiredIds === null ? null : new Set(requiredIds);
  }

  /**
   * Whether everything the first frame needs is here. Not the same question as {@link isLoaded},
   * which asks whether the whole set has finished.
   */
  isStartRequirementMet(): boolean {
    return this.required === null ? this.loaded : this.requiredOutstanding <= 0;
  }

  load(entities: E[], babylonModelService: BabylonModelService, scene: Scene) {
    this.babylonModelService = babylonModelService;
    this.entities.clear();
    if (!entities || entities.length === 0) {
      this.loaded = true;
      this.babylonModelService.handleLoaded();
      return;
    }

    this.loadingCount = entities.length;
    // Register all entities up front (getEntity must work before a model finishes loading),
    // but only start a bounded number; the rest wait in the queue.
    this.pending = entities.map(entity => {
      this.entities.set(entity.id, entity);
      return {entity, scene};
    });

    // What the gate waits for goes first. Without this the required entities would sit behind
    // whatever the load order happened to be - and the whole point is not to wait for that.
    if (this.required !== null) {
      const required = this.required;
      this.requiredOutstanding = entities.filter(entity => required.has(entity.id)).length;
      this.pending.sort((a, b) =>
        Number(required.has(b.entity.id)) - Number(required.has(a.entity.id)));
      if (this.requiredOutstanding === 0) {
        // Nothing is required, so the gate is already open. Say so now rather than at the end of
        // a load nobody is waiting for.
        this.babylonModelService.handleLoaded();
      }
    }

    const initial = Math.min(this.maxConcurrentLoads(), this.pending.length);
    for (let i = 0; i < initial; i++) {
      this.pumpNext();
    }
  }

  private pumpNext(): void {
    const next = this.pending.shift();
    if (next) {
      this.loadBabylonModel(next.entity, next.scene);
    }
  }

  isLoaded(): boolean {
    return this.loaded;
  }

  /** Whether this one entity is usable yet, regardless of what the rest of the set is doing. */
  isEntityLoaded(entityId: number): boolean {
    return this.babylonModels.has(entityId);
  }

  /**
   * Ask to be told when one entity is usable, and move it to the front of the queue while you
   * wait. Returns a cancel function - the caller may be a unit that dies, or a terrain tile that
   * scrolls away, before its model ever arrives.
   *
   * Called only for an entity that {@link isEntityLoaded} says is not ready; a caller that does
   * not check first would be waiting for a notification that has already been sent.
   */
  whenEntityLoaded(entityId: number, callback: (loaded: boolean) => void): () => void {
    let callbacks = this.waiting.get(entityId);
    if (!callbacks) {
      callbacks = [];
      this.waiting.set(entityId, callbacks);
    }
    callbacks.push(callback);
    this.prioritise(entityId);
    return () => {
      const list = this.waiting.get(entityId);
      const index = list ? list.indexOf(callback) : -1;
      if (index >= 0) {
        list!.splice(index, 1);
      }
    };
  }

  /**
   * Move a queued entity to the head of the queue. Somebody is looking at the place where this
   * model belongs, which makes it worth more than whatever the load order happened to be.
   *
   * It cannot preempt a load already in flight - a glTF parse is one synchronous block - so the
   * gain is bounded by the queue, not by the current model.
   */
  prioritise(entityId: number): void {
    const index = this.pending.findIndex(p => p.entity.id === entityId);
    if (index > 0) {
      this.pending.unshift(this.pending.splice(index, 1)[0]);
    }
  }

  getEntity(entityId: number): E {
    return this.entities.get(entityId)!
  }

  getBabylonModel(entityId: number): B {
    return this.babylonModels.get(entityId)!;
  }

  protected abstract loadBabylonModel(babylonMaterialEntity: E, scene: Scene): void;

  protected setBabylonModel(entity: E, babylonModel: B) {
    this.babylonModels.set(entity.id, babylonModel);
  }

  /**
   * @param entityId the entity that just finished, where the container tracks it. Waiters are
   *                 woken from here rather than from setBabylonModel so they never see a model
   *                 whose textures have not been assigned yet.
   */
  protected handleBabylonModelLaded(entityId?: number) {
    if (entityId !== undefined) {
      const callbacks = this.waiting.get(entityId);
      this.waiting.delete(entityId);
      const loaded = this.babylonModels.has(entityId);
      callbacks?.forEach(callback => {
        try {
          callback(loaded);
        } catch (e) {
          console.error(e);
        }
      });
      // A failed load counts too. The gate must open on "this will not arrive" exactly as it does
      // on "this arrived", or one 404 keeps every player on the splash screen forever.
      if (this.required?.has(entityId) && --this.requiredOutstanding === 0) {
        this.babylonModelService.handleLoaded();
      }
    }
    this.loadingCount--;
    if (this.loadingCount <= 0) {
      this.loaded = true;
      this.babylonModelService.handleLoaded();
    } else {
      // Keep the pipeline full: start the next queued model now that a slot freed up.
      this.pumpNext();
    }
  }
}

export class BabylonMaterialContainer extends BabylonModelContainer<BabylonMaterialEntity, Material> {
  private babylonMaterialControllerClient!: BabylonMaterialControllerClient;

  public setHttpClient(httpClient: HttpClient): void {
    this.babylonMaterialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  protected loadBabylonModel(babylonMaterialEntity: BabylonMaterialEntity, scene: Scene): void {
    this.babylonMaterialControllerClient.getData(babylonMaterialEntity.id)
      .then(data => {
        try {
          let material;
          if (babylonMaterialEntity.nodeMaterial) {
            material = NodeMaterial.Parse(data, scene, "/rest/images/");
            material.ignoreAlpha = false; // Can not be saved in the NodeEditor
          } else {
            material = Material.Parse(data, scene, "/rest/images/");
          }
          if (material) {
            this.setBabylonModel(babylonMaterialEntity, material);
          } else {
            console.error(`Error parsing material`);
          }
          // Named, so a waiter can be woken for this one material and the start gate can count it.
          // Reported without an id, every one of these looked alike and nobody could wait for a
          // single material - which is what kept all six of them in front of the first frame.
          this.handleBabylonModelLaded(babylonMaterialEntity.id);
        } catch (e) {
          console.error(e);
          console.error(`Error parsing material '${e}'`);
          this.handleBabylonModelLaded(babylonMaterialEntity.id);
        }
      })
      .catch(err => {
        console.error(`Error loading Babylon file '${err}'`);
        this.handleBabylonModelLaded(babylonMaterialEntity.id);
      })
  }
}

export class GlbContainer extends BabylonModelContainer<GltfEntity, AssetContainer> {
  private gltfHelpers: Map<number, GltfHelper> = new Map();
  diplomacyMaterialCache: Map<number, Map<Diplomacy, Map<string, NodeMaterial>>> = new Map<number, Map<Diplomacy, Map<string, NodeMaterial>>>();

  constructor(private babylonMaterialContainer: BabylonMaterialContainer, private zone: NgZone) {
    super();
  }

  // glTF parsing (mesh build + animation channels) is the heaviest main-thread work at boot,
  // so we keep only a couple in flight to leave the render loop room to breathe.
  protected override maxConcurrentLoads(): number {
    return 2;
  }

  // Coalesce progress-driven change-detection to at most one tick per animation frame: the loaders
  // run outside the Angular zone (to avoid CD storms during parsing), so the progress bar needs an
  // explicit, throttled re-entry to stay visible without reintroducing the boot freeze.
  private progressFlushScheduled = false;

  private scheduleProgressFlush(): void {
    if (this.progressFlushScheduled) {
      return;
    }
    this.progressFlushScheduled = true;
    const flush = () => {
      this.progressFlushScheduled = false;
      // Empty zone.run triggers one change-detection cycle; the template reads the latest field.
      this.zone.run(() => {});
    };
    if (typeof requestAnimationFrame === 'function') {
      requestAnimationFrame(flush);
    } else {
      setTimeout(flush, 16);
    }
  }

  public getGltfHelper(gltfEntityId: number) {
    return this.gltfHelpers.get(gltfEntityId);
  }

  protected loadBabylonModel(gltfEntity: GltfEntity, scene: Scene): void {
    const url = `${URL_GLTF}/glb/${gltfEntity.id}`;
    const gltfHelper = new GltfHelper(gltfEntity, this.babylonModelService, this, this.babylonMaterialContainer);
    this.gltfHelpers.set(gltfEntity.id, gltfHelper);
    // Run the whole load outside the Angular zone: Babylon's glTF loader fires progress/parse
    // callbacks synchronously while parsing, and each one used to trigger Angular change
    // detection (the boot-freeze amplifier). We re-enter the zone only once, on completion.
    this.zone.runOutsideAngular(() => {
      try {
        let hasError = false;
        const result = SceneLoader.LoadAssetContainer(url, '', scene, assetContainer => {
            try {
              if (!hasError) {
                // The materials this model paints itself with are no longer guaranteed to be
                // there: they were taken out of the start gate, because a 4.8 MB vehicle material
                // in front of the first frame is 4.8 MB a player waits for while owning no
                // vehicles. So the model waits for its own instead - it is the only thing that
                // knows which ones it needs, and a mesh set up without them turns bright red for
                // good. The download ran in parallel with the glb, so this is usually already
                // satisfied by the time it is asked.
                this.whenMaterialsReady(gltfEntity, () => {
                  this.setBabylonModel(gltfEntity, assetContainer);
                  this.assignGlbTextures(gltfEntity, assetContainer, gltfHelper);
                  // Single change-detection on completion: clear the progress UI and let Angular react.
                  this.zone.run(() => {
                    this.babylonModelService.glbContainerProgress = undefined;
                    this.handleBabylonModelLaded(gltfEntity.id);
                  });
                });
              }
            } catch (error) {
              console.error(error);
            }
          },
          (event: ISceneLoaderProgressEvent) => {
            // Update the field cheaply outside the zone, then flush change-detection at most once
            // per frame. Running zone.run on EVERY progress event was the original boot-freeze
            // amplifier; never running it (the regression) left the "Loading models..." bar invisible.
            this.babylonModelService.glbContainerProgress = {
              loaded: event.loaded, total: event.total
            };
            this.scheduleProgressFlush();
          },
          (scene: Scene, message: string, exception?: any) => {
            hasError = true;
            console.error(`Error loading glTF/glb '${url}'. exception: '${exception}'`);
            this.zone.run(() => this.handleBabylonModelLaded(gltfEntity.id));
          }, ".glb")
        if (result === null) {
          console.error(`Error loading glTF/glb '${url}'`);
          this.zone.run(() => this.handleBabylonModelLaded(gltfEntity.id));
        }
      } catch (e) {
        console.error(`Error loading glTF/glb '${url}'`);
        console.error(e);
        this.zone.run(() => this.handleBabylonModelLaded(gltfEntity.id));
      }
    });
  }

  /**
   * Runs {@code onReady} once every material this model names is loaded - or has failed, which is
   * also an answer: a model held back for a material that will never arrive would be invisible
   * instead of merely wrong, and wrong is the better of the two.
   * <p>
   * Waiting also pulls each material to the front of the material queue, so asking is what makes
   * it arrive sooner.
   */
  private whenMaterialsReady(gltf: GltfEntity, onReady: () => void): void {
    const missing = Object.values(gltf.materialGltfNames ?? {})
      .filter(materialId => !this.babylonMaterialContainer.isEntityLoaded(materialId));
    if (missing.length === 0) {
      onReady();
      return;
    }
    let outstanding = missing.length;
    missing.forEach(materialId =>
      this.babylonMaterialContainer.whenEntityLoaded(materialId, () => {
        if (--outstanding === 0) {
          onReady();
        }
      }));
  }

  private assignGlbTextures(gltf: GltfEntity, assetContainer: AssetContainer, gltfHelper: GltfHelper) {
    Object.keys(gltf.materialGltfNames).forEach((gltfMaterialName: string) => {
      let materialId = gltf.materialGltfNames[gltfMaterialName];
      let babylonMaterialEntity = this.babylonMaterialContainer.getEntity(materialId);
      if (babylonMaterialEntity) {
        if (babylonMaterialEntity.overrideAlbedoTextureNode
          || babylonMaterialEntity.overrideMetallicTextureNode
          || babylonMaterialEntity.overrideBumpTextureNode
          || babylonMaterialEntity.overrideAmbientOcclusionTextureNode) {
          let glbMaterial = <PBRMaterial>assetContainer.materials.find(material => material.name === gltfMaterialName);
          if (glbMaterial) {
            gltfHelper.assignTextures(babylonMaterialEntity, glbMaterial);
          } else {
            console.warn(`No material in AssetContainer ${gltfMaterialName}`)
          }
        }
      } else {
        console.warn(`BabylonMaterialEntity not found. materialId: ${materialId}`)
      }
    });
  }
}

export class ParticleSystemSetContainer extends BabylonModelContainer<ParticleSystemEntity, NodeParticleSystemSet> {
  private particleSystemControllerClient!: ParticleSystemControllerClient;

  public setHttpClient(httpClient: HttpClient): void {
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  protected loadBabylonModel(particleSystemEntity: ParticleSystemEntity, scene: Scene): void {
    this.particleSystemControllerClient.getData(particleSystemEntity.id)
      .then(data => {
        // Set before announcing: a waiter woken first would ask for the model and find nothing,
        // which is the same bug the base class warns about for glb textures.
        this.setBabylonModel(particleSystemEntity, NodeParticleSystemSet.Parse(data));
        this.handleBabylonModelLaded(particleSystemEntity.id);
      }).catch(err => {
      console.error(`Load Particle System failed (inner). '${particleSystemEntity.internalName} (${particleSystemEntity.id})' Reason: ${err}`);
      this.handleBabylonModelLaded(particleSystemEntity.id);
    }).catch(err => {
      console.error(`Load Particle System failed (outer). '${particleSystemEntity.internalName} (${particleSystemEntity.id})' Reason: ${err}`);
      this.handleBabylonModelLaded(particleSystemEntity.id);
    })
  }

}
