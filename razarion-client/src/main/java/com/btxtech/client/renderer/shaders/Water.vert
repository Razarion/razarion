attribute vec3 aVertexPosition;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNVMatrix;

varying vec3 vVertexNormal;
varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

void main(void) {
    vVertexNormal = (uNVMatrix * vec4(0.0, 0.0, 1.0, 1.0)).xyz;
    vVertexPosition = (uVMatrix * vec4(aVertexPosition, 1.0)).xyz;
    vWorldVertexPosition = aVertexPosition.xyz;

    gl_Position = uPMatrix * uVMatrix * vec4(aVertexPosition, 1.0);
}
