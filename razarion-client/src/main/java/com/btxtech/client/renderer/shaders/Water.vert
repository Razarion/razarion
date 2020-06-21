precision mediump float;

//-$$$-INCLUDE-DEFINES

attribute vec3 position;
#ifdef  RENDER_SHALLOW_WATER
attribute vec2 uv;
#endif

uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat4 normalMatrix;
uniform highp mat4 shadowMatrix;

varying vec3 vNormal;
varying vec3 vWorldVertexPosition;
varying vec3 vViewPosition;
varying vec4 vShadowCoord;
#ifdef  RENDER_SHALLOW_WATER
varying vec2 vUv;
#endif

void main(void) {
    vWorldVertexPosition = position.xyz;
    vNormal = (normalMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz;
    vViewPosition = - (viewMatrix * vec4(position, 1.0)).xyz;
    #ifdef  RENDER_SHALLOW_WATER
    vUv = uv;
    #endif
    vShadowCoord = shadowMatrix * vec4(position, 1.0);

    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}
