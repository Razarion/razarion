attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec3 aVertexTangent;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNVMatrix;

varying vec3 vVertexNormal;
varying vec3 vVertexTangent;
varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

void main(void) {
    vVertexNormal = (uNVMatrix * vec4(aVertexNormal, 1.0)).xyz;
    vVertexTangent = (uNVMatrix * vec4(aVertexTangent, 1.0)).xyz;
    vVertexPosition = (uVMatrix * vec4(aVertexPosition, 1.0)).xyz;
    vWorldVertexPosition = aVertexPosition.xyz;

    gl_Position = uPMatrix * uVMatrix * vec4(aVertexPosition, 1.0);
}
