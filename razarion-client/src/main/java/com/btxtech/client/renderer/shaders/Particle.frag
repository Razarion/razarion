precision mediump float;

varying vec3 vVertexFadeout;

uniform float uProgress;

void main(void) {
    float smallest = vVertexFadeout.x;
    if(smallest > vVertexFadeout.y) {
        smallest = vVertexFadeout.y;
    }
    if(smallest > vVertexFadeout.z) {
        smallest = vVertexFadeout.z;
    }
    smallest *= 3.0;
    gl_FragColor = vec4(0.2, 0.2, 0.2, smallest * uProgress);
}
