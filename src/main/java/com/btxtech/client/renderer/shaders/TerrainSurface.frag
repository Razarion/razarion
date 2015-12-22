precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying float vEdgePosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vSlopeFactor;
varying float vType;

uniform sampler2D uSamplerCover;
uniform sampler2D uSamplerBlender;
uniform sampler2D uSamplerGround;
uniform sampler2D uSamplerGroundBm;
uniform sampler2D uSamplerSlope;
uniform sampler2D uSamplerSlopePumpMap;
uniform sampler2D uSamplerBeach;
uniform sampler2D uSamplerBeachPumpMap;
uniform sampler2D uSamplerShadow;
uniform float uEdgeDistance;
uniform float uShadowAlpha;
uniform vec3 uLightingDirection;
uniform float diffuseWeightFactor;
uniform vec3 uAmbientColor;
uniform highp mat4 uNMatrix;
uniform float bumpMapDepthGround;
uniform float bumpMapDepthSlope;
uniform float bumpMapDepthBeach;
uniform float uSlopeSpecularHardness;
uniform float uSlopeSpecularIntensity;

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

float setupSpecularLight(vec3 correctedLigtDirection, vec3 correctedNorm) {
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

vec4 renderGround(vec3 correctedLigtDirection, float shadowFactor, vec4 splatteredColorGround, vec3 groundNorm) {
    vec4 ambient = vec4(uAmbientColor, 1.0) * splatteredColorGround;
    vec4 diffuse = vec4(max(dot(normalize(groundNorm), normalize(correctedLigtDirection)), 0.0) * shadowFactor * diffuseWeightFactor * splatteredColorGround.rgb, 1.0);
    return ambient + diffuse;
}

vec4 renderSlope(vec3 correctedLigtDirection, float shadowFactor, vec4 splatteredColorGround, vec3 groundNorm, sampler2D sampler, sampler2D samplerBumpMap, float bumpMapDepth) {
    // Norm
    vec3 correctedNorm = mix(groundNorm, bumpMapNorm(samplerBumpMap, bumpMapDepth, 128.0), vSlopeFactor);
    // Color
    vec4 colorSlope = triPlanarTextureMapping(sampler, 512.0, vec2(0,0));
    vec4 textureColor = mix(splatteredColorGround, colorSlope, vSlopeFactor);
    // Light
    vec4 ambient = vec4(uAmbientColor, 1.0) * textureColor;
    vec4 diffuseFactor = vec4(max(dot(normalize(correctedNorm), normalize(correctedLigtDirection)), 0.0) * shadowFactor * diffuseWeightFactor * textureColor.rgb, 1.0);
    float specularLight = mix(0.0, setupSpecularLight(correctedLigtDirection, correctedNorm), vSlopeFactor) * shadowFactor;
    return ambient + diffuseFactor + specularLight;
}

void main(void) {
    float shadowFactor = calculateShadowFactor();
    vec3 correctedLigtDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;
    // Terrain splatting
    float blender = triPlanarTextureMapping(uSamplerBlender, 512.0, vec2(0,0)).r;
    float blendFactor = smoothstep(vEdgePosition - uEdgeDistance, vEdgePosition + uEdgeDistance, blender);
    vec4 colorCover = triPlanarTextureMapping(uSamplerCover, 512.0, vec2(0,0));
    vec4 colorGround = triPlanarTextureMapping(uSamplerGround, 512.0, vec2(0,0));
    vec4 splatteredColorGround = mix(colorCover, colorGround, blendFactor);
    // Ground norm
    vec3 norm = bumpMapNorm(uSamplerGroundBm, bumpMapDepthGround, 512.0);
    vec3 groundNorm = mix(vVertexNormal, norm, blendFactor);

    if(vType >= 0.5 && vType <= 1.5) {
        // PLATEAU(1)
        gl_FragColor = renderSlope(correctedLigtDirection, shadowFactor, splatteredColorGround, groundNorm, uSamplerSlope, uSamplerSlopePumpMap, bumpMapDepthSlope);
    } else if(vType >= 1.5 && vType <= 2.5) {
        // BEACH(2)
        gl_FragColor = renderSlope(correctedLigtDirection, shadowFactor, splatteredColorGround, groundNorm, uSamplerBeach, uSamplerBeachPumpMap, bumpMapDepthBeach);
    } else if(vType >= 2.5) {
        // UNDER_WATER(3)
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    } else {
        // GROUND(0)
        gl_FragColor = renderGround(correctedLigtDirection, shadowFactor, splatteredColorGround, groundNorm);
    }
}