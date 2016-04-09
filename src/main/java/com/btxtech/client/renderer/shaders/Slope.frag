precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vSlopeFactor;
varying float vGroundSplatting;

uniform highp mat4 uNMatrix;
uniform vec3 uLightingDirection;
uniform float diffuseWeightFactor;
uniform vec3 uAmbientColor;
uniform sampler2D uSamplerSlopeTexture;
uniform int uSamplerSlopeTextureSize;
uniform sampler2D uSamplerBumpMapSlopeTexture;
uniform int uSamplerBumpMapSlopeTextureSize;
uniform float uBumpMapSlopeDepth;
uniform float slopeSpecularIntensity;
uniform float slopeSpecularHardness;
uniform sampler2D uGroundTopTexture;
uniform int uGroundTopTextureSize;
uniform sampler2D uGroundBottomTexture;
uniform int uGroundBottomTextureSize;
uniform sampler2D uGroundSplatting;
uniform int uGroundSplattingSize;
uniform sampler2D uGroundBottomMap;
uniform int uGroundBottomMapSize;
uniform float uGroundBottomMapDepth;
uniform float uGroundSplattingDistance;

const vec4 SPECULAR_LIGHT_COLOR = vec4(1.0, 1.0, 1.0, 1.0);
const float SLOPE_FACTOR_BIAS = 0.001;

// http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
vec4 triPlanarTextureMapping(sampler2D sampler, float size, vec2 addCoord) {
    vec3 blending = abs(vVertexNormCoord);

    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz / size + addCoord);
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz / size + addCoord);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy / size + addCoord);
    return xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;
}

vec3 bumpMapNorm(sampler2D sampler, float bumpMapDepth, float size) {
      vec3 normal = normalize(vVertexNormal);
      vec3 tangent = normalize(vVertexTangent);
      vec3 binormal = cross(normal, tangent);

      float bm0 = triPlanarTextureMapping(sampler, size, vec2(0, 0)).r;
      float bmUp = triPlanarTextureMapping(sampler, size, vec2(0.0, 1.0/size)).r;
      float bmRight = triPlanarTextureMapping(sampler, size, vec2(1.0/size, 0.0)).r;

      vec3 bumpVector = (bmRight - bm0) * tangent + (bmUp - bm0)*binormal;
      normal -= bumpMapDepth * bumpVector;
      return normalize(normal);
}

vec4 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(-correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = pow(max(dot(reflectionDirection, eyeDirection), 0.0), hardness) * intensity;
     return SPECULAR_LIGHT_COLOR * factor;
}

float setupGroundSplattingFactor() {
    float splatting = triPlanarTextureMapping(uGroundSplatting, float(uGroundSplattingSize), vec2(0,0)).r;
    float splattingFactor = (vGroundSplatting + splatting) / 2.0;
    float smoothStep = 0.5 - clamp(uGroundSplattingDistance / 2.0, 0.0, 0.5);
    return smoothstep(smoothStep, 1.0 - smoothStep, splattingFactor);
}

vec4 setupGroundColor(float splattingFactor) {
    vec4 colorTop = triPlanarTextureMapping(uGroundTopTexture, float(uGroundTopTextureSize), vec2(0,0));
    vec4 colorBottom = triPlanarTextureMapping(uGroundBottomTexture, float(uGroundBottomTextureSize), vec2(0,0));
    return mix(colorBottom, colorTop, splattingFactor);
}

vec3 setupGroundNorm(float splattingFactor) {
    vec3 bottomNorm = bumpMapNorm(uGroundBottomMap, uGroundBottomMapDepth, float(uGroundBottomMapSize));
    return mix(bottomNorm, vVertexNormal, splattingFactor);
}

void main(void) {
    vec4 textureColor;
    vec3 correctedNorm;

    if(vSlopeFactor < SLOPE_FACTOR_BIAS) {
        // Ground
        float splattingFactor = setupGroundSplattingFactor();
        textureColor = setupGroundColor(splattingFactor);
        correctedNorm = setupGroundNorm(splattingFactor);
   } else if(vSlopeFactor + SLOPE_FACTOR_BIAS > 1.0) {
        // Slope
        textureColor = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));
        correctedNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth, float(uSamplerBumpMapSlopeTextureSize));
   } else {
       // Transition
       float splattingFactor = setupGroundSplattingFactor();
       vec4 groundColor = setupGroundColor(splattingFactor);
       vec4 slopeColor = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));
       textureColor = mix(groundColor, slopeColor, vSlopeFactor);
       vec3 groundNorm = setupGroundNorm(splattingFactor);
       vec3 slopeNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth, float(uSamplerBumpMapSlopeTextureSize));
       correctedNorm = mix(groundNorm, slopeNorm, vSlopeFactor);
    }

    // Light
    vec3 correctedLightDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;
    vec4 ambient = vec4(uAmbientColor, 1.0) * textureColor;
    vec4 diffuse = vec4(max(dot(normalize(correctedNorm), normalize(correctedLightDirection)), 0.0) * diffuseWeightFactor * textureColor.rgb, 1.0);
    vec4 specular = setupSpecularLight(correctedLightDirection, correctedNorm, slopeSpecularIntensity, slopeSpecularHardness);
    gl_FragColor = ambient + diffuse + specular;
}