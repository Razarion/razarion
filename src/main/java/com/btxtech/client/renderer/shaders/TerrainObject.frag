precision mediump float;

varying vec2 vTextureCoord;
varying vec3 vLightWeighting;

uniform sampler2D uSampler;

void main(void) {
    vec4 textureColor = texture2D(uSampler, vTextureCoord.st);
    gl_FragColor = vec4(textureColor.rgb, textureColor.a);
    gl_FragColor = vec4(textureColor.rgb, 0.7);
    // gl_FragColor = vec4(vTextureCoord.s, vTextureCoord.t,0.0, 1.0);
    // gl_FragColor = vec4(0.7, 0.7, 0.7, 1.0);
    // gl_FragColor = vec4(textureColor.rgb * vLightWeighting, textureColor.a);
}

