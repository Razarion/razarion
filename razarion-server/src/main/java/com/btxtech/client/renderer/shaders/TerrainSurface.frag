precision mediump float;

varying vec4 vShadowCoord;
varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vGroundSplatting;
//Light
uniform vec3 uLightDirection;
uniform vec3 uLightDiffuse;
uniform vec3 uLightAmbient;
uniform float uLightSpecularIntensity;
uniform float uLightSpecularHardness;

uniform highp mat4 uNMatrix;
uniform float uShadowAlpha;
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
uniform sampler2D uSamplerShadow;

const vec4 SPECULAR_LIGHT_COLOR = vec4(1.0, 1.0, 1.0, 1.0);

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
      vec3 binormal = cross(normal,tangent);

      float bm0 = triPlanarTextureMapping(sampler, size, vec2(0, 0)).r;
      float bmUp = triPlanarTextureMapping(sampler, size, vec2(0.0, 1.0/size)).r;
      float bmRight = triPlanarTextureMapping(sampler, size, vec2(1.0/size, 0.0)).r;

      vec3 bumpVector = (bmRight - bm0)*tangent + (bmUp - bm0)*binormal;
      normal -= bumpMapDepth * bumpVector;
      return normalize(normal);
}

float calculateShadowFactor() {
    float zNdc = vShadowCoord.z / vShadowCoord.w;
    zNdc = zNdc * 0.5 + 0.5;

    mat4 coordCorrectionMatrix = mat4(0.5, 0.0, 0.0, 0.0,
                                 0.0, 0.5, 0.0, 0.0,
                                 0.0, 0.0, 0.5, 0.0,
                                 0.5, 0.5, 0.5, 1.0);
    vec4 coordShadowMap = coordCorrectionMatrix * vShadowCoord;
    float zMap = texture2D(uSamplerShadow, coordShadowMap.st / coordShadowMap.w).r;

    if(zMap > zNdc - 0.001) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}

vec4 renderGround(vec3 correctedLigtDirection, float shadowFactor, vec4 splatteredColorGround, vec3 groundNorm) {
    vec4 ambient = vec4(uLightAmbient, 1.0) * splatteredColorGround;
    vec4 diffuse = vec4(max(dot(normalize(groundNorm), normalize(correctedLigtDirection)), 0.0) * shadowFactor * uLightDiffuse * splatteredColorGround.rgb, 1.0);
    return ambient + diffuse;
}

vec4 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = max(pow(dot(reflectionDirection, eyeDirection), hardness), 0.0) * intensity;
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
    float shadowFactor = calculateShadowFactor();
    vec3 correctedLightDirection = normalize((uNMatrix * vec4(uLightDirection, 1.0)).xyz);

    float splattingFactor = setupGroundSplattingFactor();
    vec4 textureColor = setupGroundColor(splattingFactor);
    vec3 correctedNorm = setupGroundNorm(splattingFactor);
    vec4 specular = setupSpecularLight(correctedLightDirection, correctedNorm, uLightSpecularIntensity, uLightSpecularHardness);
    // Light
    vec4 ambient = vec4(uLightAmbient, 1.0) * textureColor;
    vec4 diffuse = vec4(max(dot(normalize(correctedNorm), -correctedLightDirection), 0.0) * uLightDiffuse * textureColor.rgb, 1.0);
    gl_FragColor = ambient + diffuse * shadowFactor + specular * shadowFactor;
}