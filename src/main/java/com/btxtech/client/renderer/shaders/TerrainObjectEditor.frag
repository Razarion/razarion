precision mediump float;

uniform int uCursorType;

void main(void) {
    if(uCursorType == 0) {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    } else if(uCursorType == 1) {
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    } else  if(uCursorType == 2){
        gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    }
}
