attribute vec3 aVertexPosition;
attribute vec3 aVertexFadeout;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

varying vec3 vVertexFadeout;

void main(void) {
    vVertexFadeout = aVertexFadeout;
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
}
