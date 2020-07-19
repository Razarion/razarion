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

//-$$$-INCLUDE-CHUNK phong functions


void main(void) {
        correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));

        gl_FragColor = vec4(phong(material, vUv), 1.0);
}
