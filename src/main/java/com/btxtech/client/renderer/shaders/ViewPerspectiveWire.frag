#extension GL_OES_standard_derivatives : enable

precision mediump float;

varying vec3 vBarycentric;

float edgeFactor(){
    vec3 d = fwidth(vBarycentric);
    vec3 a3 = smoothstep(vec3(0.0), d * 1.5, vBarycentric);
    return min(min(a3.x, a3.y), a3.z);
}

void main(void) {
   // gl_FragColor = vec4(mix(vec3(1.0, 0.0, 0.0), vec3(0.5, 0.5, 0.5), edgeFactor()) * 0.5, 1);

   if(edgeFactor() < 0.5) {
     gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
   } else {
     gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
   }
}

