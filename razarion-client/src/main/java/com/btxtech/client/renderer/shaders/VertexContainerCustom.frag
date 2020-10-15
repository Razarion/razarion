//-$$$-CHUNK uniforms-fragment BEGIN
uniform PhongMaterial material;
#ifdef  ALPHA_TO_COVERAGE
uniform float alphaToCoverage;
#endif
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
    vec4 rgba = phongAlpha(material, vUv);
    float sharpenAlpha = 1.0;
    #ifdef  ALPHA_TO_COVERAGE
    sharpenAlpha = (rgba.a - alphaToCoverage) / max(fwidth(rgba.a), 0.0001) + 0.5;
    #endif
    gl_FragColor = vec4(rgba.rgb, sharpenAlpha);
//-$$$-CHUNK main-code-fragment END
