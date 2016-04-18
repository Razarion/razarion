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
uniform float uSlopeFactorDistance;
uniform sampler2D uSlopeGroundSplatting;
uniform int uSlopeGroundSplattingSize;
uniform float uSlopeGroundSplattingBumpDepth;
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
uniform float groundSpecularIntensity;
uniform float groundSpecularHardness;
uniform bool uHasWater;
uniform float uWaterLevel;
uniform float uWaterGround;

const vec4 SPECULAR_LIGHT_COLOR = vec4(1.0, 1.0, 1.0, 1.0);
const float SLOPE_FACTOR_BIAS = 0.001;
const float SLOPE_WATER_STRIPE_FADEOUT = 2.0;
const float SLOPE_WATER_STRIPE_SPECULAR_INTENSITY_FACTOR = 5.0;
const vec3 UNDER_WATER_COLOR = vec3(1.0, 1.0, 1.0);

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
    vec3 correctedLightDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;

    vec4 textureColor;
    vec3 correctedNorm;
    vec4 specular;

    if(vSlopeFactor < SLOPE_FACTOR_BIAS) {
        // Ground
        float splattingFactor = setupGroundSplattingFactor();
        textureColor = setupGroundColor(splattingFactor);
        correctedNorm = setupGroundNorm(splattingFactor);
        specular = setupSpecularLight(correctedLightDirection, correctedNorm, groundSpecularIntensity, groundSpecularHardness);
   } else if(vSlopeFactor + SLOPE_FACTOR_BIAS > 1.0) {
        // Slope
        if(uHasWater) {
            float z = vVertexPositionCoord.z;
            if(z > uWaterLevel + SLOPE_WATER_STRIPE_FADEOUT) {
                // Over water level: render normal slope
                textureColor = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));
                correctedNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth, float(uSamplerBumpMapSlopeTextureSize));
                specular = setupSpecularLight(correctedLightDirection, correctedNorm, slopeSpecularIntensity, slopeSpecularHardness);
           } else  if(z >= uWaterLevel) {
                // Water slope stripe:
                float slopeFadeoutFactor = 1.0 - ((z - uWaterLevel) / SLOPE_WATER_STRIPE_FADEOUT);
                float colorSlopeFadeoutFactor = mix(1.0, 0.5, slopeFadeoutFactor);
                vec3 correctedLightDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;
                vec4 slopeColor = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));
                vec3 slopeNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth * (1.0 - slopeFadeoutFactor), float(uSamplerBumpMapSlopeTextureSize));
                vec4 ambient = vec4(uAmbientColor, 1.0) * slopeColor /* * colorSlopeFadeoutFactor*/;
                vec4 diffuse = vec4(max(dot(normalize(slopeNorm), normalize(correctedLightDirection)), 0.0) /* * shadowFactor */ * diffuseWeightFactor * colorSlopeFadeoutFactor * slopeColor.rgb, 1.0);
                float specularIntensity = mix(slopeSpecularIntensity, slopeSpecularIntensity * SLOPE_WATER_STRIPE_SPECULAR_INTENSITY_FACTOR, slopeFadeoutFactor);
                vec4 specular = setupSpecularLight(correctedLightDirection, slopeNorm, specularIntensity, slopeSpecularHardness) /* * shadowFactor */;
                gl_FragColor = ambient + diffuse + specular;
              return;
            } else {
                // Under water level: render slope fadeout
                float underWaterFactor = (z - uWaterGround) / (uWaterLevel - uWaterGround);
                vec3 slopeNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth, float(uSamplerBumpMapSlopeTextureSize));
                vec3 correctedLightDirection = (uNMatrix * vec4(uLightingDirection, 1.0)).xyz;
                vec3 ambient = uAmbientColor * UNDER_WATER_COLOR * underWaterFactor;
                vec3 diffuse = vec3(max(dot(normalize(slopeNorm), normalize(correctedLightDirection)), 0.0) * underWaterFactor /* * shadowFactor*/ * diffuseWeightFactor * UNDER_WATER_COLOR);
                gl_FragColor = vec4(vec3(ambient + diffuse), 1.0);
                return;
            }
        } else {
            textureColor = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));
            correctedNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth, float(uSamplerBumpMapSlopeTextureSize));
            specular = setupSpecularLight(correctedLightDirection, correctedNorm, slopeSpecularIntensity, slopeSpecularHardness);
       }
   } else {
       // Transition
       // Setup slope factor
       float slopeGroundSplatting = triPlanarTextureMapping(uSlopeGroundSplatting, float(uSlopeGroundSplattingSize), vec2(0,0)).r;
       float slopeGroundSplattingFactor = (vSlopeFactor + slopeGroundSplatting) / 2.0;
       float slopeGroundSplattinSmoothStep = 0.5 - clamp(uSlopeFactorDistance / 2.0, 0.0, 0.5);
       float correctedSlopeFactor = smoothstep(slopeGroundSplattinSmoothStep, 1.0 - slopeGroundSplattinSmoothStep, slopeGroundSplattingFactor);
       // Mix slope and ground
       float splattingFactor = setupGroundSplattingFactor();
       vec4 groundColor = setupGroundColor(splattingFactor);
       vec4 slopeColor = triPlanarTextureMapping(uSamplerSlopeTexture, float(uSamplerSlopeTextureSize), vec2(0,0));
       textureColor = mix(groundColor, slopeColor, correctedSlopeFactor);
       vec3 groundNorm = setupGroundNorm(splattingFactor);
       vec3 slopeNorm = bumpMapNorm(uSamplerBumpMapSlopeTexture, uBumpMapSlopeDepth, float(uSamplerBumpMapSlopeTextureSize));
       vec3 slopeGrounsPlattingNaorm = bumpMapNorm(uSlopeGroundSplatting, uSlopeGroundSplattingBumpDepth, float(uSlopeGroundSplattingSize));
       correctedNorm = mix(groundNorm, slopeNorm, correctedSlopeFactor) + slopeGrounsPlattingNaorm;
       vec4 specularSlope = setupSpecularLight(correctedLightDirection, correctedNorm, slopeSpecularIntensity, slopeSpecularHardness);
       vec4 specularGround = setupSpecularLight(correctedLightDirection, correctedNorm, groundSpecularIntensity, groundSpecularHardness);
       specular = mix(specularGround, specularSlope, correctedSlopeFactor);
    }

    // Light
    vec4 ambient = vec4(uAmbientColor, 1.0) * textureColor;
    vec4 diffuse = vec4(max(dot(normalize(correctedNorm), normalize(correctedLightDirection)), 0.0) * diffuseWeightFactor * textureColor.rgb, 1.0);
    gl_FragColor = ambient + diffuse + specular;
}