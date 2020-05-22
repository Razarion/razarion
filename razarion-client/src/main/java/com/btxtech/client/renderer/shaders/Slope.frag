#extension GL_OES_standard_derivatives : enable
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
vec3 correctedDirectLightDirection;

// Shadow
varying vec4 vShadowCoord;

//-$$$-INCLUDE phong struct
uniform PhongMaterial material;

//-$$$-INCLUDE phong functions

void main(void) {
    correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));

    gl_FragColor = vec4(phong(material), 1.0);
}
