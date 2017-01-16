precision mediump float;

uniform bool uHover;

void main(void) {
    if(uHover) {
       gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    } else {
       gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}
