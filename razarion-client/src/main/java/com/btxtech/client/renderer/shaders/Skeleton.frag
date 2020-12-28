precision mediump float;
//-$$$-INCLUDE-EXTENSIONS

//-$$$-INCLUDE-DEFINES

#ifdef WORLD_VERTEX_POSITION
varying vec3 vWorldVertexPosition;
#endif
varying vec3 vViewPosition;
varying vec3 vNormal;
#ifdef UV
varying vec2 vUv;
#endif
//-$$$-INCLUDE-CHUNK varyings

uniform highp mat4 viewNormMatrix;
#ifdef MODEL_MATRIX
uniform highp mat4 modelNormMatrix;
#endif

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;
vec3 correctedDirectLightDirection;

varying vec4 shadowPosition;
uniform float uShadowAlpha;
uniform sampler2D uDepthTexture;

float shadowFactor;

float calculateShadowFactor() {
    float zMap = texture2D(uDepthTexture, shadowPosition.xy).r;

    if (zMap > shadowPosition.z - 0.01) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}

struct PhongMaterial {
    sampler2D texture;
    float scale;
    sampler2D bumpMap;
    float bumpMapDepth;
    float shininess;
    float specularStrength;
};

uniform PhongMaterial topMaterial;
#ifdef  RENDER_GROUND_BOTTOM_TEXTURE
uniform PhongMaterial bottomMaterial;
struct Splatting {
    sampler2D texture;
    float scale1;
    float scale2;
    float blur;
    float offset;
};
uniform Splatting splatting;
#endif

vec3 vec3ToRgb(vec3 normVec) {
    return normVec * 0.5 + 0.5;
}

//-$$$-INCLUDE-CHUNK uniforms-fragment

vec2 dHdxy_fwd(sampler2D bumpMap, float bumpMapDepth, float textureScale, vec2 uv) {
    vec2 uvScalled = uv / textureScale;
    vec2 dSTdx = dFdx(uvScalled);
    vec2 dSTdy = dFdy(uvScalled);
    float Hll = bumpMapDepth * texture2D(bumpMap, uvScalled).x;
    float dBx = bumpMapDepth * texture2D(bumpMap, uvScalled + dSTdx).x - Hll;
    float dBy = bumpMapDepth * texture2D(bumpMap, uvScalled + dSTdy).x - Hll;
    return vec2(dBx, dBy);
}

vec3 perturbNormalArb(vec3 surf_pos, vec3 surf_norm, vec2 dHdxy) {
    vec3 vSigmaX = vec3(dFdx(surf_pos.x), dFdx(surf_pos.y), dFdx(surf_pos.z));
    vec3 vSigmaY = vec3(dFdy(surf_pos.x), dFdy(surf_pos.y), dFdy(surf_pos.z));
    vec3 vN = surf_norm;
    vec3 R1 = cross(vSigmaY, vN);
    vec3 R2 = cross(vN, vSigmaX);
    float fDet = dot(vSigmaX, R1);
    fDet *= (float(gl_FrontFacing) * 2.0 - 1.0);
    vec3 vGrad = sign(fDet) * (dHdxy.x * R1 + dHdxy.y * R2);
    return normalize(abs(fDet) * surf_norm - vGrad);
}

vec4 phongAlpha(PhongMaterial phongMaterial, vec2 uv) {
    vec3 normal = perturbNormalArb(-vViewPosition, normalize(vNormal), dHdxy_fwd(phongMaterial.bumpMap, phongMaterial.bumpMapDepth, phongMaterial.scale, uv));
    vec3 viewDir = normalize(vViewPosition);

    vec4 texture = texture2D(phongMaterial.texture, uv / phongMaterial.scale);
    vec3 diffuse = max(dot(normal, correctedDirectLightDirection), 0.0) * directLightColor;
    vec3 halfwayDir = normalize(correctedDirectLightDirection + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), phongMaterial.shininess);
    vec3 specular = phongMaterial.specularStrength * spec * directLightColor;
    #ifdef RECEIVE_SHADOW
    return vec4((ambientLightColor + diffuse * shadowFactor) * texture.rgb + specular * shadowFactor, texture.a);
    #else
    return vec4((ambientLightColor + diffuse) * texture.rgb + specular, texture.a);
    #endif
}

vec3 phong(PhongMaterial phongMaterial, vec2 uv) {
    return phongAlpha(phongMaterial, uv).rgb;
}

    #ifdef WORLD_VERTEX_POSITION
vec3 ground() {
    vec3 top = phong(topMaterial, vWorldVertexPosition.xy);
    #ifndef RENDER_GROUND_BOTTOM_TEXTURE
    return top;
    #endif

    #ifdef  RENDER_GROUND_BOTTOM_TEXTURE
    vec3 bottom = phong(bottomMaterial, vWorldVertexPosition.xy);

    float splatting1 = texture2D(splatting.texture, vWorldVertexPosition.xy / splatting.scale1).r;
    float splatting2 = texture2D(splatting.texture, vWorldVertexPosition.xy / splatting.scale2).r;
    float splattingFactor = (splatting1 + splatting2) / 2.0;
    splattingFactor = (splattingFactor - splatting.offset) / (2.0 * splatting.blur) + 0.5;
    splattingFactor = clamp(splattingFactor, 0.0, 1.0);
    return mix(top, bottom, splattingFactor);
    #endif
}
    #endif

//-$$$-INCLUDE-CHUNK code-fragment

void main(void) {
    shadowFactor = calculateShadowFactor();
    #ifdef MODEL_MATRIX
    correctedDirectLightDirection = -(normalize((viewNormMatrix * modelNormMatrix * vec4(directLightDirection, 1.0)).xyz));
    #else
    correctedDirectLightDirection = -(normalize((viewNormMatrix * vec4(directLightDirection, 1.0)).xyz));
    #endif

    //-$$$-INCLUDE-CHUNK main-code-fragment
}
