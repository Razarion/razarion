attribute vec3 aVertexPosition;

uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

void main(void) {
    gl_Position = uPMatrix * uVMatrix * vec4(aVertexPosition, 1.0);
}
