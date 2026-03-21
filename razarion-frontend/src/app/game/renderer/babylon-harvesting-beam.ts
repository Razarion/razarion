import {Color3, Mesh, RawTexture, StandardMaterial, Vector3, VertexData} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";
import {BabylonBuildupEffect} from "./babylon-buildup-effect";

export class BabylonHarvestingBeam {
  private beam: Mesh | null = null;
  private beamMaterial: StandardMaterial | null = null;
  private beamTarget: Vector3 | null = null;
  private beamStartTime: number = 0;
  private beamRenderCallback: (() => void) | null = null;

  constructor(
    private scene: Scene,
    private getBeamOrigin: () => Vector3 | null,
    private getContainerPosition: () => Vector3
  ) {
  }

  isActive(): boolean {
    return this.beamRenderCallback !== null;
  }

  start(target: Vector3): void {
    if (this.beamRenderCallback) {
      return;
    }

    try {
      this.beamTarget = target;
      this.beamStartTime = Date.now();

      this.beamRenderCallback = () => this.update();
      this.scene.registerBeforeRender(this.beamRenderCallback);
    } catch (e) {
      console.error(e);
    }
  }

  dispose(): void {
    if (this.beamRenderCallback) {
      this.scene.unregisterBeforeRender(this.beamRenderCallback);
      this.beamRenderCallback = null;
    }
    if (this.beam) {
      this.beam.dispose();
      this.beam = null;
    }
    if (this.beamMaterial) {
      this.beamMaterial.dispose();
      this.beamMaterial = null;
    }
    this.beamTarget = null;
  }

  private update(): void {
    const startPos = this.getBeamOrigin() ?? this.getContainerPosition();

    if (!this.beamTarget) {
      return;
    }

    const endPos = this.beamTarget.clone();

    if (Vector3.Distance(startPos, endPos) < 0.01) {
      return;
    }

    if (!this.beamMaterial) {
      const beamTex = BabylonBuildupEffect.createBeamTexture(this.scene);
      const beamMat = new StandardMaterial("HarvestingBeamMat", this.scene);
      beamMat.emissiveColor = new Color3(0, 3, 3);
      beamMat.diffuseColor = new Color3(0, 0, 0);
      beamMat.disableLighting = true;
      beamMat.backFaceCulling = false;
      beamMat.useEmissiveAsIllumination = true;
      beamMat.emissiveTexture = beamTex;
      beamMat.opacityTexture = beamTex;
      this.beamMaterial = beamMat;
    }

    const dist = Vector3.Distance(startPos, endPos);
    const dir = endPos.subtract(startPos).normalize();
    const angle = Math.atan2(dir.x, dir.z);

    const narrowWidth = 0.4;
    const wideWidth = 1.2;

    if (!this.beam) {
      this.beam = new Mesh("HarvestingBeam", this.scene);
      this.beam.isPickable = false;
      this.beam.material = this.beamMaterial;
    }

    const vertexData = new VertexData();
    vertexData.positions = [
      -narrowWidth / 2, 0, 0,
      narrowWidth / 2, 0, 0,
      wideWidth / 2, dist, 0,
      -wideWidth / 2, dist, 0,
    ];
    vertexData.indices = [0, 1, 2, 0, 2, 3];
    vertexData.uvs = [0, 0, 1, 0, 1, 1, 0, 1];
    vertexData.normals = [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1];
    vertexData.applyToMesh(this.beam, true);

    this.beam.position = startPos.clone();
    this.beam.rotationQuaternion = null;
    this.beam.rotation.set(Math.PI / 2, angle, 0);

    // Scroll texture along beam (energy flowing from resource to harvester)
    const elapsed = (Date.now() - this.beamStartTime) / 1000;
    (this.beamMaterial.emissiveTexture as any).vOffset = elapsed * 1.5;
  }
}
