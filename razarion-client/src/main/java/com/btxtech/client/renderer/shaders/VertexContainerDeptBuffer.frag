precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D uTexture;

void main(void) {
    vec4 textureColor = texture2D(uTexture, vTextureCoord.st);
    if(textureColor.a < 0.5) {
        discard;
    } else {
        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }
}

