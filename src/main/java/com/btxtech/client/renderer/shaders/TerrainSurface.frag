#extension GL_OES_standard_derivatives : enable

precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying float vEdgePosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;

uniform sampler2D uSamplerCover;
uniform sampler2D uSamplerBlender;
uniform sampler2D uSamplerGround;
uniform sampler2D uSamplerGroundBm;
uniform sampler2D uSamplerSlope;
uniform sampler2D uSamplerShadow;
uniform sampler2D uSamplerSlopePumpMap;
uniform float uEdgeDistance;
uniform float uShadowAlpha;
uniform vec3 uLightingDirection;
uniform float diffuseWeightFactor;
uniform vec3 uAmbientColor;
uniform highp mat4 uNMatrix;
uniform float bumpMapDepthSlope;
uniform float bumpMapDepthGround;
uniform float uSlopeSpecularHardness;
uniform float uSlopeSpecularIntensity;
uniform float slopeTopThreshold;
uniform float slopeTopThresholdFading;

const vec3 LIGHT_COLOR = vec3(1.0, 1.0, 1.0);
const float PLATEAU_GROUND = 0.0;
const float PLATEAU_TOP = 100.0;
const float PLATEAU_GROUND_CHANGE = 20.0;


// http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
vec4 triPlanarTextureMapping(sampler2D sampler, float scale, vec2 addCoord) {
    vec3 blending = abs(vVertexNormCoord);
    blending = normalize(max(blending, 0.00001)); // Force weights to sum to 1.0
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz / scale + addCoord);
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz / scale + addCoord);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy / scale + addCoord);
    // blend the results of the 3 planar projections.
    return xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;
}

vec3 bumpMapNorm(sampler2D sampler, float bumpMapDepth, float scale) {
      vec3 normal = normalize(vVertexNormal);
      vec3 tangent = normalize(vVertexTangent);
      vec3 binormal = cross(normal,tangent);

      float bm0 = triPlanarTextureMapping(sampler, scale, vec2(0, 0)).r;
      float bmUp = triPlanarTextureMapping(sampler, scale, vec2(0.0, 1.0/scale)).r;
      float bmRight = triPlanarTextureMapping(sampler, scale, vec2(1.0/scale, 0.0)).r;

      vec3 bumpVector = (bmRight - bm0)*tangent + (bmUp - bm0)*binormal;
      normal -= bumpMapDepth * bumpVector;
      return normalize(normal);
}

float setupSpecularLightFactor(vec3 correctedLigtDirection, vec3 correctedNorm) {
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     vec3 reflectionDirection = normalize(reflect(-correctedLigtDirection, correctedNorm));
     return pow(max(dot(reflectionDirection, eyeDirection), 0.0), uSlopeSpecularHardness) * uSlopeSpecularIntensity;
}

float calculateShadowFactor() {
    // Shadow
    float zNdc = vShadowCoord.z / vShadowCoord.w;
    mat4 coordCorrectionMatrix = mat4(0.5, 0.0, 0.0, 0.0,
                                 0.0, 0.5, 0.0, 0.0,
                                 0.0, 0.0, 0.5, 0.0,
                                 0.5, 0.5, 0.5, 1.0);
    vec4 coordShadowMap = coordCorrectionMatrix * vShadowCoord;
    float zMap = texture2D(uSamplerShadow, coordShadowMap.st / coordShadowMap.w).r;
    zNdc = zNdc * 0.5 + 0.5;
    if(zMap > zNdc - 0.01) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}

float calculateGroundFactor() {
    if(vVertexPositionCoord.z == PLATEAU_TOP) {
       return 0.0;
    } else if(vVertexPositionCoord.z == PLATEAU_GROUND) {
       return 0.0;
    } else {
       if(vVertexPositionCoord.z > (PLATEAU_TOP - PLATEAU_GROUND_CHANGE)) {
           return (PLATEAU_TOP - vVertexPositionCoord.z) / PLATEAU_GROUND_CHANGE;
       } else if(vVertexPositionCoord.z < PLATEAU_GROUND_CHANGE) {
           return vVertexPositionCoord.z / PLATEAU_GROUND_CHANGE;
       }  else {
           return 1.0;
       }
    }
}

void main(void) {
    float shadowFactor = calculateShadowFactor();

    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;

    vec4 colorSlope = triPlanarTextureMapping(uSamplerSlope, 512.0, vec2(0,0));

    //// remove
    vec3 correctedZenith = (uNMatrix * vec4(vec3(0, 0, 1), 1.0)).xyz;
    float slope = dot(vVertexNormal.xyz, correctedZenith);
    float _slopeFactor = smoothstep(slopeTopThreshold + slopeTopThresholdFading, slopeTopThreshold - slopeTopThresholdFading, slope);
    //// remove ends

    float slopeFactor = calculateGroundFactor();

    vec3 correctedNorm = mix(bumpMapNorm(uSamplerGroundBm, bumpMapDepthGround, 512.0), bumpMapNorm(uSamplerSlopePumpMap, bumpMapDepthSlope, 128.0), slopeFactor);

    // float blender = texture2D(uSamplerBlender, vVertexPositionCoord.xy / 512.0).r;
    float blender = triPlanarTextureMapping(uSamplerBlender, 512.0, vec2(0,0)).r;
    float blendFactor = smoothstep(vEdgePosition - uEdgeDistance, vEdgePosition + uEdgeDistance, blender);
    // vec4 colorCover = texture2D(uSamplerCover, vVertexPositionCoord.xy / 512.0);
    vec4 colorCover = triPlanarTextureMapping(uSamplerCover, 512.0, vec2(0,0));
    // vec4 colorGround = texture2D(uSamplerGround, vVertexPositionCoord.xy / 512.0);
    vec4 colorGround = triPlanarTextureMapping(uSamplerGround, 512.0, vec2(0,0));
    vec4 splatteredColorGround = mix(colorCover, colorGround, blendFactor);

    vec3 correctedNorm2;
    if(slopeFactor == 0.0) {
        correctedNorm2 = mix(vVertexNormal, correctedNorm, blendFactor);
    } else {
        correctedNorm2 = correctedNorm;
    }

    vec4 textureColor = mix(splatteredColorGround, colorSlope, slopeFactor);
    float specularLightFactor = mix(0.0, setupSpecularLightFactor(correctedLigtDirection, correctedNorm2), slopeFactor);

    // Diffuse light
    vec4 diffuseFactor = vec4(max(dot(normalize(correctedNorm2), normalize(correctedLigtDirection)), 0.0) * shadowFactor * diffuseWeightFactor * LIGHT_COLOR , 1.0);
    vec4 ambientDiffuseFactor = diffuseFactor + vec4(uAmbientColor, 1.0);
    gl_FragColor = textureColor * ambientDiffuseFactor + vec4(specularLightFactor * shadowFactor * LIGHT_COLOR, 1.0);

    // gl_FragColor = vec4(coverColorFactor, coverColorFactor, coverColorFactor, 1.0);


    // float heightFactor = vVertexPositionCoord.z / 100.0;
    // gl_FragColor = vec4(heightFactor, heightFactor, heightFactor, 1.0);

//     float x = calculateGroundFactor();
//     gl_FragColor = vec4(x, x, x, 1.0);

}