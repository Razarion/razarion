precision mediump float;

varying vec2 vUv;

uniform sampler2D uDepthSampler;
uniform sampler2D uColorSampler;
uniform bool uDepthMap;

void main(void) {
    if (uDepthMap) {
        float z = texture2D(uDepthSampler, vUv.st).r;
        gl_FragColor = vec4(z, z, z, 1.0);
    } else {
        gl_FragColor = texture2D(uColorSampler, vUv.st);
    }
}

