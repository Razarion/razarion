precision mediump float;

uniform int uCursorType;

void main(void) {
    if(uCursorType == 0) {
        // CREATE
        gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
    } else if(uCursorType == 1) {
        // MODIFY
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    } else  if(uCursorType == 2){
        // REMOVE_MODE
        gl_FragColor = vec4(1.0, 0.8, 0.8, 1.0);
     } else {
         gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
   }
}
