precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D uDeepSampler;
uniform sampler2D uColorSampler;
uniform bool uDeepMap;

void main(void) {
    // gl_FragColor = vec4(texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t)).rgb, 1.0);
    // float n = 1.0;
    // float f = 2000.0;
    // float z = texture2D(uSampler, vTextureCoord.st).r;
    // float grey = (2.0 * n) / (f + n - z*(f-n));
    // vec4 color = vec4(grey, grey, grey, 1.0);
    // gl_FragColor = color;

    if(uDeepMap) {
        float z = texture2D(uDeepSampler, vTextureCoord.st).r;
        gl_FragColor = vec4(z, z, z, 1.0);
   } else {
        gl_FragColor = vec4(texture2D(uColorSampler, vTextureCoord.st).rgb, 1.0);
   }


    // vec4 color = vec4(texture2D(uColorSampler, vec2(vTextureCoord.s, vTextureCoord.t)).rgb, 1.0);
    // float z = texture2D(uDeepSampler, vTextureCoord.st).r;
    // vec4 deep = vec4(z, z, z, 1.0);
    // gl_FragColor = mix(color, deep, 0.5);
}

