precision mediump float;

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
// Shadow
varying vec4 vShadowCoord;

//-$$$-INCLUDE-CHUNK phong struct
uniform PhongMaterial material;

//-$$$-INCLUDE-CHUNK phong functions

void main(void) {
    gl_FragColor = phong(material);
}