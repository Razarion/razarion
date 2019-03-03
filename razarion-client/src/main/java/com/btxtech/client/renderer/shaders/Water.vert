attribute vec3 aVertexPosition;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;
uniform highp mat4 uNVMatrix;

varying vec3 vVertexPosition;
varying vec3 vWorldVertexPosition;

void main(void) {
    vVertexPosition = (uVMatrix * vec4(aVertexPosition, 1.0)).xyz;
    vWorldVertexPosition = aVertexPosition.xyz;

    gl_Position = uPMatrix * vec4(vVertexPosition, 1.0);
}
