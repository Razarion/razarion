attribute vec3 aVertexPosition;
attribute vec3 aBarycentric;
attribute vec2 aTextureCoord;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

varying vec3 vBarycentric;
varying vec2 vTextureCoord;

void main(void) {
    vBarycentric = aBarycentric;
    vTextureCoord = aTextureCoord;
    gl_Position = uPMatrix * uVMatrix * vec4(aVertexPosition, 1.0);
}

