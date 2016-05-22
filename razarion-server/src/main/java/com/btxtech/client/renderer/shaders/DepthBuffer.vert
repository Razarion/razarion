attribute vec3 aVertexPosition;
attribute vec3 aBarycentric;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

varying vec3 vBarycentric;

void main(void) {
    vBarycentric = aBarycentric;

    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
}
