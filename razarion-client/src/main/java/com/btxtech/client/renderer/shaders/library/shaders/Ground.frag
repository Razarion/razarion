//-$$$-CHUNK variables BEGIN
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
//-$$$-CHUNK variables END

//-$$$-CHUNK functions BEGIN
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
//-$$$-CHUNK functions END
