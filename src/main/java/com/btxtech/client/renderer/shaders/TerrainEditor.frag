precision mediump float;

uniform bool uSelected;

void main(void) {
    if(uSelected) {
       gl_FragColor = vec4(1.0, 0.0, 00, 1.0);
    } else {
       gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}
