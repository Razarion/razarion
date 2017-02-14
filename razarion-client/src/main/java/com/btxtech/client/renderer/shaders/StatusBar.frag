precision mediump float;

uniform vec4 uColor;
uniform vec4 uBgColor;
uniform float uProgress;

varying float vVisibility;

void main(void) {
   if(vVisibility < uProgress) {
       gl_FragColor = uColor;
   } else {
       gl_FragColor = uBgColor;
   }
}
