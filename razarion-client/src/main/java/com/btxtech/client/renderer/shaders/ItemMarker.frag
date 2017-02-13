precision mediump float;

uniform vec4 uColor;
uniform float uRadius;

varying float vVisibility;

const float THICKNESS = 0.3;

void main(void) {
   if(vVisibility > (1.0 - THICKNESS / uRadius)) {
       gl_FragColor = uColor;
   } else {
       discard;
   }
}
