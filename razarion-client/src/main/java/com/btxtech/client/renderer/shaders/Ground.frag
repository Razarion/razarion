precision mediump float;
#extension GL_OES_standard_derivatives : enable

varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec3 vNormal;
uniform highp mat4 normalMatrix;

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;
// Shadow
varying vec4 vShadowCoord;


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
vec3 correctedDirectLightDirection;

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


vec3 phong(PhongMaterial phongMaterial) {
    vec3 normal = perturbNormalArb(-vViewPosition, normalize(vNormal), dHdxy_fwd(phongMaterial.bumpMap, phongMaterial.bumpMapDepth, phongMaterial.scale));
    vec3 viewDir = normalize(vViewPosition);

    vec4 texture = texture2D(phongMaterial.texture, vWorldVertexPosition.xy / phongMaterial.scale);
    vec3 diffuse = max(dot(normal, correctedDirectLightDirection), 0.0) * directLightColor;
    vec3 halfwayDir = normalize(correctedDirectLightDirection + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), phongMaterial.shininess);
    vec3 specular = phongMaterial.specularStrength * spec * directLightColor;
    return (ambientLightColor + diffuse) * texture.rgb + specular;
}

void main(void) {
    correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));

    vec3 top = phong(topMaterial);
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
