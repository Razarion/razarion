attribute vec3 aVertexPosition;
attribute float aVisibility;

uniform highp mat4 uMMatrix;
uniform highp mat4 uVMatrix;
uniform highp mat4 uPMatrix;

varying float vVisibility;

void main(void) {
    vVisibility = aVisibility;
    gl_Position = uPMatrix * uVMatrix * uMMatrix * vec4(aVertexPosition, 1.0);
}
