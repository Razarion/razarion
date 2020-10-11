
//-$$$-CHUNK attributes BEGIN
attribute float slopeFactor;
//-$$$-CHUNK attributes END

//-$$$-CHUNK varyings BEGIN
varying float vSlopeFactor;
//-$$$-CHUNK varyings END

//-$$$-CHUNK main-code-vertex BEGIN
    vSlopeFactor = slopeFactor;
//-$$$-CHUNK main-code-vertex END

//----------------------------------------------------------------------

//-$$$-CHUNK uniforms-fragment BEGIN
uniform PhongMaterial material;

#ifdef RENDER_GROUND_TEXTURE
#ifdef RENDER_SPLATTING
// Slope-Ground Splatting
struct SlopeSplatting {
    sampler2D texture;
    float scale;
    float impact;
    float blur;
    float offset;
};
uniform SlopeSplatting slopeSplatting;
#endif
#endif
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
vec3 slope = phong(material, vUv);

#ifdef RENDER_GROUND_TEXTURE
float splatting = clamp(vSlopeFactor, 0.0, 1.0);
#ifdef RENDER_SPLATTING
float splattingTexture = texture2D(slopeSplatting.texture, vWorldVertexPosition.xy / slopeSplatting.scale).r;
splatting = (splattingTexture * slopeSplatting.impact + vSlopeFactor) / (1.0 + slopeSplatting.impact);
splatting = (splatting - slopeSplatting.offset) / (2.0 * slopeSplatting.blur) + 0.5;
splatting = clamp(splatting, 0.0, 1.0);
#endif
vec3 slopeGround = mix(ground(), slope, splatting);
gl_FragColor = vec4(slopeGround, 1.0);
#else
gl_FragColor = vec4(slope, 1.0);
#endif
//-$$$-CHUNK main-code-fragment END
