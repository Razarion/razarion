precision mediump float;

attribute vec3 position;
attribute vec3 objectNormal;

uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat3 normalMatrix;
uniform highp mat4 shadowMatrix;

varying vec3 vNormal;
varying vec3 vViewPosition;
varying vec3 vWorldVertexPosition;
varying vec4 vShadowCoord;

void main(void) {
    vWorldVertexPosition = position.xyz;
    vNormal = normalize(normalMatrix * objectNormal);
    vViewPosition = - (viewMatrix * vec4(position, 1.0)).xyz;
    vShadowCoord = shadowMatrix * vec4(position, 1.0);

    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}

