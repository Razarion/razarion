precision mediump float;

varying vec2 vTextureCoord;
varying float buildupZ;

uniform sampler2D uSampler;
uniform float progressZ;

void main(void) {
    vec4 textureColor = texture2D(uSampler, vTextureCoord.st);
    if(buildupZ > progressZ) {
        discard;
    } else if(textureColor.a < 0.5) {
        discard;
    } else {
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    }
}
