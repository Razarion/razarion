#extension GL_OES_standard_derivatives : enable
precision mediump float;

//-$$$-INCLUDE-DEFINES

varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec3 vNormal;
varying vec2 vUv;
varying float vSlopeFactor;
uniform highp mat4 normalMatrix;

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;
vec3 correctedDirectLightDirection;
// Shadow
varying vec4 vShadowCoord;

//-$$$-INCLUDE-CHUNK phong struct
uniform PhongMaterial material;

//-$$$-INCLUDE-CHUNK phong functions

#ifdef  RENDER_GROUND_TEXTURE
//-$$$-INCLUDE-CHUNK ground variables

//-$$$-INCLUDE-CHUNK ground functions

#ifdef RENDER_SPLATTING
// Slope-Ground Splatting
struct SlopeSplatting {
    sampler2D texture;
    float scale1;
    float impact;
    float blur;
    float offset;
};
uniform SlopeSplatting slopeSplatting;
#endif
#endif

void main(void) {
    correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));

    vec3 slope = phong(material, vUv);

    #ifdef RENDER_GROUND_TEXTURE
    float splatting = clamp(vSlopeFactor, 0.0, 1.0);
    #ifdef RENDER_SPLATTING
    float splattingTexture = texture2D(slopeSplatting.texture, vWorldVertexPosition.xy / slopeSplatting.scale1).r;
    splatting = (splattingTexture * slopeSplatting.impact + vSlopeFactor) / (1.0 + slopeSplatting.impact);
    splatting = (splatting - slopeSplatting.offset) / (2.0 * slopeSplatting.blur) + 0.5;
    splatting = clamp(slopeSplatting, 0.0, 1.0);
    #endif
    vec3 slopeGround = mix(ground(), slope, splatting);
    gl_FragColor = vec4(slopeGround, 1.0);
    #else
    gl_FragColor = vec4(slope, 1.0);
    #endif
}
