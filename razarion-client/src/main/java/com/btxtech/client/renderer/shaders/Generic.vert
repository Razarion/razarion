precision mediump float;

//-$$$-INCLUDE-DEFINES

attribute vec3 position;
#ifndef FIX_OBJECT_NORMAL
attribute vec3 objectNormal;
#endif
#ifdef UV
attribute vec2 uv;
#endif

#ifdef MODEL_MATRIX
uniform highp mat4 modelMatrix;
#endif
uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;
uniform highp mat4 normalMatrix;
uniform highp mat4 shadowMatrix;

varying vec3 vNormal;
varying vec3 vViewPosition;
#ifdef UV
varying vec2 vUv;
#endif
varying vec4 shadowPosition;
#ifdef WORLD_VERTEX_POSITION
varying vec3 vWorldVertexPosition;
#endif

void main(void) {
    #ifdef FIX_OBJECT_NORMAL
    vNormal = (normalMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz;
    #else
    vNormal = (normalMatrix * vec4(objectNormal, 1.0)).xyz;
    #endif

    #ifdef UV
    vUv = uv;
    #endif

    shadowPosition = shadowMatrix * vec4(position, 1.0);

    #ifdef WORLD_VERTEX_POSITION
    vWorldVertexPosition = position.xyz;
    #endif

    #ifdef MODEL_MATRIX
    vViewPosition = - (viewMatrix * modelMatrix * vec4(position, 1.0)).xyz;
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    #else
    vViewPosition = - (viewMatrix * vec4(position, 1.0)).xyz;
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    #endif
}
