//-$$$-CHUNK uniforms-fragment BEGIN
uniform bool uHover;
uniform bool uDelete;
//-$$$-CHUNK uniforms-fragment END


//-$$$-CHUNK main-code-fragment BEGIN
if (uHover) {
    if (uDelete) {
        gl_FragColor = vec4(1.0, 0.8, 0.8, 1.0);
    } else {
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    }
} else {
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
//-$$$-CHUNK main-code-fragment END
