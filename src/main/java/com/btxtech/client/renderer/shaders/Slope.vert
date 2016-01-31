precision mediump float;

attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec3 aVertexTangent;
attribute float aSlopeFactor;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNMatrix;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying float vSlopeFactor;

void main(void) {
    vVertexNormal = (uNMatrix * vec4(aVertexNormal, 1.0)).xyz;
    vVertexTangent = (uNMatrix * vec4(aVertexTangent, 1.0)).xyz;
    gl_Position = uPMatrix * uVMatrix * vec4(aVertexPosition, 1.0);
    vSlopeFactor = aSlopeFactor;
}

