attribute vec3 aVertexPosition;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

void main(void) {
    gl_Position = uPMatrix * uVMatrix * uMMatrix *vec4(aVertexPosition, 1.0);
}
