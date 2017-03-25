precision mediump float;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec4 vVertexPosition;
varying vec3 vVertexPositionCoord;
varying vec3 vVertexNormCoord;
varying float vGroundSplatting;
// Light
uniform vec3 uLightDirection;
uniform vec3 uLightDiffuse;
uniform vec3 uLightAmbient;
uniform float uLightSpecularIntensity;
uniform float uLightSpecularHardness;
// Shadow
varying vec4 vShadowCoord;
uniform float uShadowAlpha;
uniform sampler2D uShadowTexture;

uniform highp mat4 uNVMatrix;
uniform sampler2D uTopTexture;
uniform float uTopTextureScale;
uniform sampler2D uTopBm;
uniform float uTopBmScale;
uniform float uTopBmOnePixel;
uniform float uTopBmDepth;
uniform sampler2D uBottomTexture;
uniform float uBottomTextureScale;
uniform sampler2D uSplatting;
uniform float uSplattingScale;
uniform sampler2D uBottomBm;
uniform float uBottomBmScale;
uniform float uBottomBmOnePixel;
uniform float uBottomBmDepth;

const vec3 SPECULAR_LIGHT_COLOR = vec3(1.0, 1.0, 1.0);
const float BIAS = 0.001;

// http://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821
vec4 triPlanarTextureMapping(sampler2D sampler, float scale, vec2 addCoord) {
    vec3 blending = abs(vVertexNormCoord);

    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    vec4 xAxisTop = texture2D(sampler, vVertexPositionCoord.yz * scale + addCoord);
    vec4 yAxisTop = texture2D(sampler, vVertexPositionCoord.xz * scale + addCoord);
    vec4 zAxisTop = texture2D(sampler, vVertexPositionCoord.xy * scale + addCoord);
    return xAxisTop * blending.x + yAxisTop * blending.y + zAxisTop * blending.z;
}

vec3 bumpMapNorm(sampler2D sampler, float bumpMapDepth, float scale, float onePixel) {
    vec3 normal = normalize(vVertexNormal);
    vec3 tangent = normalize(vVertexTangent);
    vec3 binormal = cross(normal, tangent);

    float bm0 = triPlanarTextureMapping(sampler, scale, vec2(0, 0)).r;
    float bmUp = triPlanarTextureMapping(sampler, scale, vec2(0.0, onePixel)).r;
    float bmRight = triPlanarTextureMapping(sampler, scale, vec2(onePixel, 0.0)).r;

    vec3 bumpVector = (bm0 - bmRight) * tangent + (bm0 - bmUp) * binormal;
    return normalize(normal + bumpMapDepth * bumpVector);
}

float calculateShadowFactor() {
    float zMap = texture2D(uShadowTexture, vShadowCoord.st).r;

    if(zMap > vShadowCoord.z - 0.001) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}

vec4 setupSpecularLight(vec3 correctedLightDirection, vec3 correctedNorm, float intensity, float hardness) {
     vec3 reflectionDirection = normalize(reflect(correctedLightDirection, normalize(correctedNorm)));
     vec3 eyeDirection = normalize(-vVertexPosition.xyz);
     float factor = pow(max(dot(reflectionDirection, eyeDirection), 0.0), hardness) * intensity;
     return vec4(SPECULAR_LIGHT_COLOR * factor, 1.0);
}

void main(void) {
    float shadowFactor = calculateShadowFactor();
    vec3 correctedLightDirection = normalize((uNVMatrix * vec4(uLightDirection, 1.0)).xyz);

    vec4 colorTop = triPlanarTextureMapping(uTopTexture, uTopTextureScale, vec2(0,0));
    vec3 normTop = bumpMapNorm(uTopBm, uTopBmDepth, uTopBmScale, uTopBmOnePixel);
    vec4 colorBottom = triPlanarTextureMapping(uBottomTexture, uBottomTextureScale, vec2(0,0));
    vec3 normBottom = bumpMapNorm(uBottomBm, uBottomBmDepth, uBottomBmScale, uBottomBmOnePixel);
    float splatting = triPlanarTextureMapping(uSplatting, uSplattingScale, vec2(0,0)).r;

     vec3 norm;
     vec4 textureColor;

    // Bottom top splatting
    if(vGroundSplatting + BIAS >= 1.0) {
        norm = normTop;
        textureColor = colorTop;
    } else if(vGroundSplatting <= BIAS) {
        norm = normBottom;
        textureColor = colorBottom;
    } else {
        float topBmValue = triPlanarTextureMapping(uTopBm, uTopBmScale, vec2(0,0)).r;

        if(topBmValue + splatting > vGroundSplatting) {
            norm = normTop;
            textureColor = colorTop;
        } else {
            norm = normBottom;
            textureColor = colorBottom;
        }
    }

    // Light
    vec4 ambient = vec4(uLightAmbient, 1.0) * textureColor;
    vec4 diffuse = vec4(max(dot(norm, -correctedLightDirection), 0.0) * uLightDiffuse * textureColor.rgb, 1.0);
    vec4 specular = setupSpecularLight(correctedLightDirection, norm, uLightSpecularIntensity, uLightSpecularHardness);
    gl_FragColor = ambient + diffuse * shadowFactor + specular * shadowFactor;
}


////------------------------------------------------------------------------------------------
//// Patches are pumped out
////------------------------------------------------------------------------------------------
//    if(vGroundSplatting + BIAS >= 1.0) {
//        norm = normTop;
//        textureColor = colorTop;
//    } else if(vGroundSplatting <= BIAS) {
//        norm = normBottom;
//        textureColor = colorBottom;
//    } else {
//        float delta = 0.05;
//        float step = vGroundSplatting;
//        if(splatting > step + delta) {
//            norm = normTop;
//            textureColor = colorTop;
//        } else if(splatting < step - delta){
//            norm = normBottom;
//            textureColor = colorBottom;
//        } else {
//            vec3 slpattingNorm = bumpMapNorm(uSplatting, 10.0, uSplattingScale);
//
//            float y = (splatting + delta - step) / (2.0 * delta);
//            norm = mix(slpattingNorm, normTop, y);
//            // textureColor = mix(colorBottom, colorTop, y);
//            textureColor = colorTop;
//
////            if(y > 1.0) {
////                gl_FragColor = vec4(1.0, 0.0, 0.0,1.0);
////            } else if(y < 0.0) {
////                gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
////            } else {
////                gl_FragColor = vec4(y, y, y, 1.0);
////            }
////            return;
//
//        }
//    }
////------------------------------------------------------------------------------------------
//// ENDS Patches are pumped out ENDS
////------------------------------------------------------------------------------------------
