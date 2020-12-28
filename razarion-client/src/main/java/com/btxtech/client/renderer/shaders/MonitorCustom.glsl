//-$$$-CHUNK uniforms-fragment BEGIN
uniform sampler2D uDepthSampler;
uniform sampler2D uColorSampler;
uniform bool uDepthMap;
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
    if (uDepthMap) {
        float z = texture2D(uDepthSampler, vUv.st).r;
        gl_FragColor = vec4(z, z, z, 1.0);
    } else {
        gl_FragColor = texture2D(uColorSampler, vUv.st);
    }
//-$$$-CHUNK main-code-fragment END

