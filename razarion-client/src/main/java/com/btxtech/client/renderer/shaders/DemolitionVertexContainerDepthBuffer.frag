precision mediump float;

varying vec2 vTextureCoord;
varying float height;

uniform sampler2D uTexture;
uniform float uHealth;
uniform float uMaxHeight;

void main(void) {
    vec4 textureColor = texture2D(uTexture, vTextureCoord.st);
    if(height > uMaxHeight * uHealth) {
        discard;
    } else if(textureColor.a < 0.5) {
        discard;
    } else {
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    }
}
