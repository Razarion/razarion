precision mediump float;
#extension GL_OES_standard_derivatives : enable

varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec3 vNormal;
// Light
const vec3 DIRECTIONAL_LIGHT_COLOR = vec3(0.8, 0.8, 0.8);
const vec3 DIRECTIONAL_LIGHT_DIRECTION = vec3(0.0, 0.0, -1.0);
const vec3 AMBIENT_LIGTH_COLOR = vec3(0.2, 0.2, 0.2);
// Shadow
varying vec4 vShadowCoord;
// Top
uniform sampler2D topMaterialTexture;
uniform float topMaterialScale;
uniform sampler2D topMaterialBumpMap;
uniform float topMaterialBumpMapDepth;
uniform float topMaterialShininess;
uniform float topMaterialSpecularStrength;
#ifdef  RENDER_GROUND_BOTTOM_TEXTURE
// Bottom
uniform sampler2D uBottomTexture;
uniform float uBottomTextureScale;
uniform sampler2D uBottomBumpMap;
uniform float uBottomBumpMapDepth;
uniform float uBottomShininess;
uniform float uBottomSpecularStrength;
// Top-Bottom Splatting
uniform sampler2D uSplatting;
uniform float uSplattingScale1;
uniform float uSplattingScale2;
uniform float uSplattingFadeThreshold;
uniform float uSplattingOffset;
#endif

vec3 vec3ToReg(vec3 normVec) {
    return normVec * 0.5 + 0.5;
}

vec2 dHdxy_fwd(sampler2D uBumpMap, float uBumpMapDepth, float uTextureScale) {
    vec2 vUv = vWorldVertexPosition.xy / uTextureScale;
    vec2 dSTdx = dFdx(vUv);
    vec2 dSTdy = dFdy(vUv);
    float Hll = uBumpMapDepth * texture2D(uBumpMap, vUv).x;
    float dBx = uBumpMapDepth * texture2D(uBumpMap, vUv + dSTdx).x - Hll;
    float dBy = uBumpMapDepth * texture2D(uBumpMap, vUv + dSTdy).x - Hll;
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


vec3 phong(sampler2D uTexture, float uTextureScale, sampler2D uBumpMap, float uBumpMapDepth, float uShininess, float uSpecularStrength) {
    vec3 normal = perturbNormalArb(-vViewPosition, normalize(vNormal), dHdxy_fwd(uBumpMap, uBumpMapDepth, uTextureScale));
    vec3 viewDir = normalize(vViewPosition);
    vec3 directLightColor = DIRECTIONAL_LIGHT_COLOR;
    vec3 directLightDirection = DIRECTIONAL_LIGHT_DIRECTION;

    vec4 texture = texture2D(uTexture, vWorldVertexPosition.xy / uTextureScale);
    vec3 diffuse = max(dot(normal, directLightDirection), 0.0) * directLightColor;
    vec3 halfwayDir = normalize(directLightDirection + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), uShininess);
    vec3 specular = uSpecularStrength * spec * directLightColor;
    return (AMBIENT_LIGTH_COLOR + diffuse) * texture.rgb + specular;
}

void main(void) {
    vec3 top = phong(topMaterialTexture, topMaterialScale, topMaterialBumpMap, topMaterialBumpMapDepth, topMaterialShininess, topMaterialSpecularStrength);
    #ifndef RENDER_GROUND_BOTTOM_TEXTURE
    gl_FragColor = vec4(top, 1.0);
    #endif

    #ifdef  RENDER_GROUND_BOTTOM_TEXTURE
    vec3 bottom = phong(uBottomTexture, uBottomTextureScale, uBottomBumpMap, uBottomBumpMapDepth, uBottomShininess, uBottomSpecularStrength);

    float splatting1 = texture2D(uSplatting, vWorldVertexPosition.xy / uSplattingScale1).r;
    float splatting2 = texture2D(uSplatting, vWorldVertexPosition.xy / uSplattingScale2).r;
    float splatting = (splatting1 + splatting2) / 2.0;
    splatting = (splatting - uSplattingOffset) / (2.0 * uSplattingFadeThreshold) + 0.5;
    splatting = clamp(splatting, 0.0, 1.0);
    gl_FragColor = vec4(mix(top, bottom, splatting), 1.0);
    #endif
}
