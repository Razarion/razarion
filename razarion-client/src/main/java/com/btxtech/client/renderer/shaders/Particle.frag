precision mediump float;

varying vec3 vVertexFadeout;

uniform float uProgress;
uniform sampler2D uColorRamp;

void main(void) {
    float fadeout = vVertexFadeout.x;
    if(fadeout > vVertexFadeout.y) {
        fadeout = vVertexFadeout.y;
    }
    if(fadeout > vVertexFadeout.z) {
        fadeout = vVertexFadeout.z;
    }
    fadeout *= 3.0;

    float yOffset = clamp(uProgress, 0.001, 0.999);
    vec4 color = texture2D(uColorRamp, vec2(0.05, yOffset));
    gl_FragColor = vec4(color.r, color.g, color.b, fadeout * color.a);
}
