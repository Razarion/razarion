precision mediump float;

uniform vec4 uColor;
uniform float uProgress;

void main(void) {
   gl_FragColor = vec4(0.5, 0.5, 0.5, uProgress);
}
