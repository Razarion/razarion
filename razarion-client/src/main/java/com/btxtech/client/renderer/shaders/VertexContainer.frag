#extension GL_OES_standard_derivatives : enable
precision mediump float;

//-$$$-INCLUDE-DEFINES

varying vec3 vViewPosition;
varying vec3 vNormal;
varying vec2 vUv;
uniform highp mat4 normalMatrix;

// Light
uniform vec3 directLightDirection;
uniform vec3 directLightColor;
uniform vec3 ambientLightColor;
vec3 correctedDirectLightDirection;

//-$$$-INCLUDE-CHUNK phong struct
uniform PhongMaterial material;
#ifdef  ALPHA_TO_COVERAGE
uniform float alphaToCoverage;
#endif

//-$$$-INCLUDE-CHUNK phong functions


void main(void) {
    correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));

    vec4 rgba = phongAlpha(material, vUv);
    float sharpenAlpha = 1.0;
    #ifdef  ALPHA_TO_COVERAGE
    sharpenAlpha = (rgba.a - alphaToCoverage) / max(fwidth(rgba.a), 0.0001) + 0.5;
    #endif
    gl_FragColor = vec4(rgba.rgb, sharpenAlpha);
}
