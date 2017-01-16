precision mediump float;

uniform bool uHover;
uniform bool uDelete;

void main(void) {
     if(uHover) {
         if(uDelete) {
             gl_FragColor = vec4(1.0, 0.8, 0.8, 1.0);
         } else {
            gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
         }
     }else {
         gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
     }
}
