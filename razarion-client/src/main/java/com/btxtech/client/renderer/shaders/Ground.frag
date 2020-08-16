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

//-$$$-INCLUDE-CHUNK phong struct

//-$$$-INCLUDE-CHUNK ground variables

//-$$$-INCLUDE-CHUNK phong functions

//-$$$-INCLUDE-CHUNK ground functions

// Shadow
varying vec4 shadowPosition;
uniform float uShadowAlpha;
uniform sampler2D uDepthTexture;


float calculateShadowFactor() {
    float zMap = texture2D(uDepthTexture, shadowPosition.xy).r;

    if (zMap > shadowPosition.z - 0.01) {
        return 1.0;
    } else {
        return uShadowAlpha;
    }
}



void main(void) {
    correctedDirectLightDirection = -(normalize((normalMatrix * vec4(directLightDirection, 1.0)).xyz));
    float shadow = calculateShadowFactor();
    gl_FragColor = vec4(shadow * ground(), 1.0);
}
