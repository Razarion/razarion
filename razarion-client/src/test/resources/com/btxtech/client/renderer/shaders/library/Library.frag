//-$$$-CHUNK struct BEGIN
struct PhongMaterial {
    sampler2D texture;
    float scale;
    sampler2D bumpMap;
    float bumpMapDepth;
    float shininess;
    float specularStrength;
};
//-$$$-CHUNK struct END

//-$$$-CHUNK functions BEGIN
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
//-$$$-CHUNK functions END
