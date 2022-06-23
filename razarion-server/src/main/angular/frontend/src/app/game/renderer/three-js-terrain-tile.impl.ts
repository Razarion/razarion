import {
  SlopeConfig,
  SlopeGeometry,
  SlopeSplattingConfig,
  TerrainTile,
  ThreeJsTerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {
  BufferAttribute,
  BufferGeometry,
  Group,
  Material,
  Matrix4,
  Mesh,
  MeshBasicMaterial,
  RepeatWrapping,
  Scene,
  ShaderMaterial,
  TextureLoader,
  WebGLRenderTarget
} from "three";
import {ThreeJsModelService} from "./three-js-model.service";
import {getImageUrl} from "../../common";

const splattingSlopeVertexShader = `
attribute float slopeFactor;

varying vec2 vUv;
varying float vSlopeFactor;
varying vec3 vWorldVertexPosition;

void main(void) {
    #include <beginnormal_vertex>

    vSlopeFactor = slopeFactor;

    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);

    vUv = gl_Position.xy / gl_Position.w;
    vWorldVertexPosition = position.xyz;
}
`
const splattingSlopeFragmentShader = `
varying vec2 vUv;
varying float vSlopeFactor;
varying vec3 vWorldVertexPosition;

uniform sampler2D slope;
uniform sampler2D ground;

uniform sampler2D splattingImage;
uniform float scale;
uniform float impact;
uniform float blur;
uniform float offset;

void main(void) {
    vec2 uVNdcSpace = vUv * 0.5 + 0.5;
    vec4 slopeTexture = texture2D(slope, uVNdcSpace);
    vec4 groundTexture = texture2D(ground, uVNdcSpace);

    // float splatting = clamp(vSlopeFactor, 0.0, 1.0);

    float splattingTexture = texture2D(splattingImage, vWorldVertexPosition.xy / scale).r;
    float splatting = (splattingTexture * impact + vSlopeFactor) / (1.0 + impact);
    splatting = (splatting - offset) / (2.0 * blur) + 0.5;
    splatting = clamp(splatting, 0.0, 1.0);

    vec3 slopeGround = mix(groundTexture, slopeTexture, splatting).rgb;
    gl_FragColor = vec4(slopeGround, 1.0);
}
`

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
  private group = new Group();
  private slopeGroup = new Group();
  private slopeInnerGroundGroup = new Group();

  constructor(terrainTile: TerrainTile,
              private defaultGroundConfigId: number,
              private scene: Scene,
              private slopeScene: Scene,
              private slopeInnerGroundScene: Scene,
              private slopeRenderTarget: WebGLRenderTarget,
              private slopeInnerGroundRenderTarget: WebGLRenderTarget,
              private gwtAngularService: GwtAngularService,
              private threeJsModelService: ThreeJsModelService) {
    this.group.name = `TerrainTile ${terrainTile.getIndex().toString()}`;
    if (terrainTile.getGroundTerrainTiles() !== null) {
      terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
        let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
        // Geometry
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(groundTerrainTile.positions, 3));
        geometry.setAttribute('normal', new BufferAttribute(groundTerrainTile.norms, 3));
        geometry.setAttribute('uv', this.uvFromPosition(groundTerrainTile.positions));
        let material;
        if (groundConfig.getTopThreeJsMaterial() === undefined || groundConfig.getTopThreeJsMaterial() == null) {
          material = new MeshBasicMaterial({color: 0x11EE11});
          material.wireframe = true;
          console.warn(`No top material in GroundConfig ${groundConfig.getInternalName()} '${groundConfig.getId()}'`);
        } else {
          material = threeJsModelService.getMaterial(groundConfig.getTopThreeJsMaterial());
        }
        const cube = new Mesh(geometry, material);
        cube.name = "Ground"
        cube.receiveShadow = true;
        this.group.add(cube);
      });
    }
    if (terrainTile.getTerrainSlopeTiles() !== null) {
      const _this = this;
      terrainTile.getTerrainSlopeTiles().forEach(terrainSlopeTile => {
        try {
          let slopeConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getSlopeConfig(terrainSlopeTile.slopeConfigId);
          if (slopeConfig.getThreeJsMaterial() === undefined) {
            throw new Error(`SlopeConfig has no threeJsMaterial: ${slopeConfig.toString()}`);
          }
          let material = threeJsModelService.getMaterial(slopeConfig.getThreeJsMaterial());
          if (terrainSlopeTile.outerSlopeGeometry !== null && terrainSlopeTile.outerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(terrainSlopeTile.outerSlopeGeometry, material, _this.setGroundMaterial(null), slopeConfig.getOuterSlopeSplattingConfig());
          }
          if (terrainSlopeTile.centerSlopeGeometry !== null && terrainSlopeTile.centerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(terrainSlopeTile.centerSlopeGeometry, material, null, null);
          }
          if (terrainSlopeTile.innerSlopeGeometry !== null && terrainSlopeTile.innerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(terrainSlopeTile.innerSlopeGeometry, material, _this.setGroundMaterial(slopeConfig), slopeConfig.getInnerSlopeSplattingConfig());
          }
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(error);
        }
      });
    }
    if (terrainTile.getTerrainWaterTiles() !== null) {
      terrainTile.getTerrainWaterTiles().forEach(terrainWaterTile => {
        if (terrainWaterTile.positions !== null && terrainWaterTile.positions !== undefined) {
          this.setupWater(terrainWaterTile.positions);
        }
        if (terrainWaterTile.shallowPositions !== null && terrainWaterTile.shallowPositions !== undefined) {
          this.setupShallowWater(terrainWaterTile.shallowPositions, terrainWaterTile.shallowUvs);
        }
      });
    }
    if (terrainTile.getTerrainTileObjectLists() !== null) {
      const _this = this;
      terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
        try {
          let terrainObjectConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
          if (terrainObjectConfig.getThreeJsUuid() === undefined) {
            throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          }
          terrainTileObjectList.models.forEach(model => {
            let m = model.getColumnMajorFloat32Array();
            let matrix4 = new Matrix4();
            matrix4.set(
              m[0], m[4], m[8], m[12],
              m[1], m[5], m[9], m[13],
              m[2], m[6], m[10], m[14],
              m[3], m[7], m[11], m[15]
            );
            let threeJsModel = threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsUuid());
            threeJsModel.applyMatrix4(matrix4);
            // threeJsModel.position.x = 100;

            // object3D.traverse((innerObject3D: Object3D) => {
            // innerObject3D.matrixAutoUpdate = true;
            // });
            // object3D.traverse((innerObject3D: Object3D) => {
            //     if (innerObject3D.type === 'SkinnedMesh') {
            //         console.info(innerObject3D.type)
            //         // object3D.position.x = 10;
            //         // object3D.position.y = 20;
            //         innerObject3D.scale.x = 10;
            //         innerObject3D.scale.y = 10;
            //         innerObject3D.scale.z = 10;
            //         innerObject3D.translateX(20);
            //         innerObject3D.updateMatrix();
            //         innerObject3D.updateWorldMatrix(true, true);
            //         // innerObject3D.applyMatrix4(matrix4);
            //     }
            // });

            _this.group.add(threeJsModel);
          });
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(terrainTileObjectList);
          console.error(error);
        }
      });
    }
  }

  addToScene(): void {
    this.scene.add(this.group);
    this.slopeScene.add(this.slopeGroup);
    this.slopeInnerGroundScene.add(this.slopeInnerGroundGroup);
  }

  removeFromScene(): void {
    this.scene.remove(this.group);
    this.slopeScene.remove(this.slopeGroup);
    this.slopeInnerGroundScene.remove(this.slopeInnerGroundGroup);
  }

  private setGroundMaterial(slopeConfig: SlopeConfig | null): Material {
    if (slopeConfig && slopeConfig.getGroundConfigId()) {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(slopeConfig.getGroundConfigId()).getTopThreeJsMaterial();
      return this.threeJsModelService.getMaterial(innerGroundConfigMaterialId);
    } else {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(this.defaultGroundConfigId).getTopThreeJsMaterial();
      return this.threeJsModelService.getMaterial(innerGroundConfigMaterialId);
    }
  }

  private uvFromPosition(positions: Float32Array) {
    let uvs = new Float32Array(positions.length * 2 / 3);
    let uvCount = uvs.length / 2;
    for (let uvIndex = 0; uvIndex < uvCount; uvIndex++) {
      uvs[uvIndex * 2] = positions[uvIndex * 3];
      uvs[uvIndex * 2 + 1] = positions[uvIndex * 3 + 1];
    }
    return new BufferAttribute(uvs, 2);
  }

  private setupSlopeGeometry(slopeGeometry: SlopeGeometry, material: Material, groundMaterial: Material | null, splatting: SlopeSplattingConfig | null): void {
    if (groundMaterial && splatting) {
      let splattingGeometry = new BufferGeometry();
      splattingGeometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
      splattingGeometry.setAttribute('normal', new BufferAttribute(slopeGeometry.norms, 3));
      splattingGeometry.setAttribute('uv', this.uvFromPosition(slopeGeometry.positions));
      splattingGeometry.setAttribute('slopeFactor', new BufferAttribute(slopeGeometry.slopeFactors, 1));
      let splattingMaterial = new ShaderMaterial({
        uniforms: {
          slope: {value: this.slopeRenderTarget.texture},
          ground: {value: this.slopeInnerGroundRenderTarget.texture},

          splattingImage: {value: null},
          scale: {value: splatting.getScale()},
          impact: {value: splatting.getImpact()},
          blur: {value: splatting.getBlur()},
          offset: {value: splatting.getOffset()},
        },
        vertexShader: splattingSlopeVertexShader,
        fragmentShader: splattingSlopeFragmentShader
      });
      splattingMaterial.uniforms.splattingImage.value = new TextureLoader().load(getImageUrl(splatting.getTextureId()));
      splattingMaterial.uniforms.splattingImage.value.wrapS = RepeatWrapping;
      splattingMaterial.uniforms.splattingImage.value.wrapT = RepeatWrapping;

      let splattingSlope = new Mesh(splattingGeometry, splattingMaterial);
      splattingSlope.name = "Slope-Splatted";
      this.group.add(splattingSlope);

      let groundGeometry = new BufferGeometry();
      groundGeometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
      groundGeometry.setAttribute('normal', new BufferAttribute(slopeGeometry.norms, 3));
      groundGeometry.setAttribute('uv', this.uvFromPosition(slopeGeometry.positions));
      let groundMaterialClone = groundMaterial.clone();
      groundMaterialClone.transparent = true;
      let groundSlope = new Mesh(groundGeometry, groundMaterialClone);
      groundSlope.name = "Slope-Ground";
      this.slopeInnerGroundGroup.add(groundSlope);

      let geometry = new BufferGeometry();
      geometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
      geometry.setAttribute('normal', new BufferAttribute(slopeGeometry.norms, 3));
      geometry.setAttribute('uv', new BufferAttribute(slopeGeometry.uvs, 2));
      let slope = new Mesh(geometry, material);
      slope.name = "Slope";
      this.slopeGroup.add(slope);
    } else {
      let geometry = new BufferGeometry();
      geometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
      geometry.setAttribute('normal', new BufferAttribute(slopeGeometry.norms, 3));
      geometry.setAttribute('uv', new BufferAttribute(slopeGeometry.uvs, 2));
      let slope = new Mesh(geometry, material);
      slope.name = "Slope";
      this.group.add(slope);
    }

    // const meshNormalMaterial = new MeshNormalMaterial()
    // meshNormalMaterial.normalMap = (<any>material).normalMap;
    // if ((<any>material).map && meshNormalMaterial.normalMap) {
    //   meshNormalMaterial.normalMap.repeat = (<any>material).map.repeat;
    //   if ((<any>material).normalScale) {
    //     meshNormalMaterial.normalScale = (<any>material).normalScale
    //   }
    // }
    // const slope = new Mesh(geometry, meshNormalMaterial);

    // const meshBasicMaterial = new MeshBasicMaterial({ color: 0x8888ff });
    // const slope = new Mesh(geometry, meshBasicMaterial);

    // const normHelper = new VertexNormalsHelper( slope, 2, 0x00ff00);
    // this.group.add(normHelper);
  }

  private setupWater(positions: Float32Array) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(positions, 3));
    const material = new MeshBasicMaterial({color: 0x0000ff});
    material.wireframe = true;
    const cube = new Mesh(geometry, material);
    cube.name = "Water";
    this.group.add(cube);
  }

  private setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(shallowPositions, 3));
    geometry.setAttribute('uvs', new BufferAttribute(shallowUvs, 3));
    const material = new MeshBasicMaterial({color: 0x5555ff});
    material.wireframe = true;
    const cube = new Mesh(geometry, material);
    cube.name = "Shallow Water";
    this.group.add(cube);
  }

}
