import {Injectable} from "@angular/core";
import {TerrainWaterTile, WaterConfig} from "../../gwtangular/GwtAngularFacade";
import {
  BufferAttribute,
  BufferGeometry,
  Color,
  CubeTextureLoader,
  Group,
  Mesh,
  MultiplyOperation,
  RepeatWrapping,
  ShaderLib,
  ShaderMaterial,
  TangentSpaceNormalMap,
  TextureLoader,
  UniformsUtils,
  Vector2,
  Vector3
} from "three";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {getGwtMockImageUrl} from "./game-mock.service";
import {SignalGenerator} from "../signal-generator";
import {Texture} from "three/src/textures/Texture";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {ThreeJsModelService} from "./three-js-model.service";

export const vertex = /* glsl */`
#define PHONG

varying vec3 vViewPosition;

#ifdef  RENDER_SHALLOW_WATER
attribute vec2 shallowUv;
varying vec2 vShallowUv;
#endif

#include <common>
#include <uv_pars_vertex>
#include <uv2_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>

void main() {

	#include <uv_vertex>
	#include <uv2_vertex>
	#include <color_vertex>

	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>

	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>

	vViewPosition = - mvPosition.xyz;

#ifdef  RENDER_SHALLOW_WATER
  vShallowUv = shallowUv;
#endif

	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}
`;

export const fragment = /* glsl */`
#define PHONG

uniform vec3 diffuse;
uniform vec3 emissive;
uniform vec3 specular;
uniform float shininess;
uniform float opacity;

uniform float uDistortionStrength;
uniform float uDistortionAnimation;

#ifdef  RENDER_SHALLOW_WATER
varying vec2 vShallowUv;
uniform sampler2D uShallowWater;
uniform float uShallowWaterScale;
uniform sampler2D uShallowDistortionMap;
uniform float uShallowDistortionStrength;
uniform float uShallowAnimation;
uniform sampler2D uWaterStencil;
#endif

#include <common>
#include <packing>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <uv2_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_phong_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>

void main() {

	#include <clipping_planes_fragment>

	vec4 diffuseColor = vec4( diffuse, opacity );
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;

	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	vec3 mapN1 = texture2D( normalMap, vUv / uDistortionStrength + vec2(uDistortionAnimation, 0.5)).xyz * 2.0 - 1.0;
	vec3 mapN2 = texture2D( normalMap, vUv / uDistortionStrength + vec2(-uDistortionAnimation, uDistortionAnimation)).xyz * 2.0 - 1.0;
  vec3 mapN = (mapN1 + mapN2) / 2.0;
	mapN.xy *= normalScale;
	normal = perturbNormal2Arb( - vViewPosition, normal, mapN, faceDirection );
	#include <emissivemap_fragment>

	// accumulation
	#include <lights_phong_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>

	// modulation
	#include <aomap_fragment>

	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + reflectedLight.directSpecular + reflectedLight.indirectSpecular + totalEmissiveRadiance;

	#include <envmap_fragment>
	#include <output_fragment>
	#include <tonemapping_fragment>
	#include <encodings_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>

#ifdef  RENDER_SHALLOW_WATER
  vec2 totalShallowDistortion = uShallowDistortionStrength  * (texture2D(uShallowDistortionMap, vShallowUv / uShallowWaterScale + vec2(uShallowAnimation, 0)).rg * 2.0 - 1.0);
  vec4 shallowWater = texture2D(uShallowWater, (vShallowUv + totalShallowDistortion) / uShallowWaterScale);
  float waterStencil = texture2D(uWaterStencil, (vShallowUv + totalShallowDistortion) / uShallowWaterScale).b;
  // Porter-Duff Composition
  // https://de.wikipedia.org/wiki/Alpha_Blending
  float transparency = shallowWater.a + (1.0 - shallowWater.a) * waterStencil;
  vec3 color = 1.0 / transparency * (shallowWater.a * shallowWater.rgb + (1.0 - shallowWater.a) * waterStencil * gl_FragColor.rgb);
  gl_FragColor = vec4(color, max(shallowWater.a, min(transparency, gl_FragColor.a)));
#endif
}
`;

@Injectable()
export class ThreeJsWaterRenderService {
  private materials: { material: ShaderMaterial, waterConfig: WaterConfig }[] = [];

  constructor(private gwtAngularService: GwtAngularService, private threeJsModelService: ThreeJsModelService) {
  }

  private static getWaterAnimation(millis: number, durationSeconds: number): number {
    return SignalGenerator.sawtooth(millis, durationSeconds * 1000.0, 0);
  }

  private static setupWaterGeometry(positions: Float32Array) {
    let geometry = new BufferGeometry();
    geometry.setAttribute('position', new BufferAttribute(positions, 3));
    geometry.setAttribute('normal', ThreeJsTerrainTileImpl.fillVec3(new Vector3(0, 0, 1), positions.length));
    geometry.setAttribute('uv', ThreeJsTerrainTileImpl.uvFromPosition(positions));
    return geometry;
  }

  private static setupWaterMaterial(waterConfig: WaterConfig): ShaderMaterial {
    let cubeTexture = new CubeTextureLoader()
      .setPath('')
      .load([
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png'),
        getGwtMockImageUrl('WaterCloudReflection.png')
      ]);

    let normalMap = new TextureLoader().load(getGwtMockImageUrl('WaterNorm.png'));
    normalMap.wrapS = RepeatWrapping;
    normalMap.wrapT = RepeatWrapping;

    let waterMaterial = new ShaderMaterial({
      lights: true,
      vertexShader: vertex,
      fragmentShader: fragment,
      transparent: true,
      uniforms: UniformsUtils.merge([
        ShaderLib.phong.uniforms,
        {
          normalScale: {value: new Vector2(waterConfig.getNormalMapDepth(), waterConfig.getNormalMapDepth())},
          uDistortionStrength: {value: waterConfig.getDistortionStrength()},
          uDistortionAnimation: {value: 1.0},
          opacity: {value: waterConfig.getTransparency()},
          shininess: {value: waterConfig.getShininess()},
          specular: {value: new Color(waterConfig.getSpecularStrength(), waterConfig.getSpecularStrength(), waterConfig.getSpecularStrength())},
        }
      ]),
    });

    waterMaterial.uniforms.envMap.value = cubeTexture;
    waterMaterial.uniforms.normalMap.value = normalMap;

    (<any>waterMaterial).envMap = waterMaterial.uniforms.envMap.value;
    (<any>waterMaterial).combine = MultiplyOperation;
    (<any>waterMaterial).normalMap = waterMaterial.uniforms.normalMap.value;
    (<any>waterMaterial).normalMapType = TangentSpaceNormalMap;
    return waterMaterial;
  }

  public setup(terrainWaterTiles: TerrainWaterTile[], group: Group): void {
    if (!terrainWaterTiles) {
      return;
    }
    terrainWaterTiles.forEach(terrainWaterTile => {
      let slopeConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getSlopeConfig(terrainWaterTile.slopeConfigId);
      let waterConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getWaterConfig(slopeConfig.getWaterConfigId());


      if (terrainWaterTile.positions) {
        this.setupWater(terrainWaterTile.positions, waterConfig, group);
      }
      if (terrainWaterTile.shallowPositions) {
        this.setupShallowWater(terrainWaterTile.shallowPositions, terrainWaterTile.shallowUvs, waterConfig, group);
      }
    });
  }

  public update() {
    this.materials.forEach(value => {
      let material = value.material;
      let waterConfig = value.waterConfig;
      material.uniforms.uDistortionAnimation.value = ThreeJsWaterRenderService.getWaterAnimation(Date.now(), waterConfig.getDistortionAnimationSeconds());
      if (material.uniforms.uShallowAnimation) {
        material.uniforms.uShallowAnimation.value = ThreeJsWaterRenderService.getWaterAnimation(Date.now(), 20); // TODO Take time from SlopeConfig
      }
    })
  }

  private setupWater(positions: Float32Array, waterConfig: WaterConfig, group: Group) {
    let geometry = ThreeJsWaterRenderService.setupWaterGeometry(positions);
    let waterMaterial = ThreeJsWaterRenderService.setupWaterMaterial(waterConfig);
    this.createAndAddMesh(geometry, waterMaterial, waterConfig, group, "Water");
  }

  private setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array, waterConfig: WaterConfig, group: Group) {
    let geometry = ThreeJsWaterRenderService.setupWaterGeometry(shallowPositions);
    geometry.setAttribute('shallowUv', new BufferAttribute(shallowUvs, 2));
    let waterMaterial = ThreeJsWaterRenderService.setupWaterMaterial(waterConfig);
    waterMaterial.defines['RENDER_SHALLOW_WATER'] = '';

    waterMaterial.uniforms.uShallowWater = {value: new TextureLoader().load(getGwtMockImageUrl('Foam.png'), (texture: Texture) => texture.wrapT = RepeatWrapping)}// TODO Take time from SlopeConfig
    waterMaterial.uniforms.uShallowWaterScale = {value: 24} // TODO Take time from SlopeConfig
    waterMaterial.uniforms.uShallowDistortionMap = {
      value: new TextureLoader().load(getGwtMockImageUrl('FoamDistortion.png'), (texture: Texture) => {// TODO Take time from SlopeConfig
        texture.wrapS = RepeatWrapping;
        texture.wrapT = RepeatWrapping;
      })
    }
    waterMaterial.uniforms.uShallowDistortionStrength = {value: 1} // TODO Take time from SlopeConfig
    waterMaterial.uniforms.uShallowAnimation = {value: 1} // TODO Take time from SlopeConfig
    waterMaterial.uniforms.uWaterStencil = {value: new TextureLoader().load(getGwtMockImageUrl('WaterStencil.png'), (texture: Texture) => texture.wrapT = RepeatWrapping)}// TODO Take time from SlopeConfig

    this.createAndAddMesh(geometry, waterMaterial, waterConfig, group, "Shallow Water");
  }

  private createAndAddMesh(geometry: BufferGeometry, waterMaterial: ShaderMaterial, waterConfig: WaterConfig,group: Group, meshName: string) {
    const mesh = new Mesh(geometry, waterMaterial);

    mesh.addEventListener('added', () => {
      this.materials.push({material: waterMaterial, waterConfig: waterConfig});
    });
    mesh.addEventListener('removed', () => {
      this.materials.forEach((value, index) => {
        if (value.material == waterMaterial) {
          this.materials.splice(index, 1);
        }
      });
    });

    // const normalMaterial = new MeshNormalMaterial();
    // const mesh = new Mesh(geometry, normalMaterial);

    mesh.name = meshName;
    group.add(mesh);

    // const normHelper = new VertexNormalsHelper(mesh, 2, 0x111111);
    // group.add(normHelper);
  }
}
