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
vec3 correctedDirectLightDirection;

//-$$$-INCLUDE-DEFINES

//-$$$-INCLUDE-CHUNK shadow uniforms-and-code

//-$$$-INCLUDE-CHUNK phong struct

//-$$$-INCLUDE-CHUNK ground variables

//-$$$-INCLUDE-CHUNK phong functions

//-$$$-INCLUDE-CHUNK ground functions
void main(void) {
    //-$$$-INCLUDE-CHUNK shadow chunk
    correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));
    gl_FragColor = vec4(ground(), 1.0);
}
