attribute vec3 position;

uniform highp mat4 viewMatrix;
uniform highp mat4 projectionMatrix;

void main(void) {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}
