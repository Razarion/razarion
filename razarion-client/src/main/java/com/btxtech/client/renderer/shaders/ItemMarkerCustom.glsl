//-$$$-CHUNK varyings BEGIN
varying float vVisibility;
//-$$$-CHUNK varyings END

// --------------------------------------------------------------------------

//-$$$-CHUNK attributes BEGIN
attribute float aVisibility;
//-$$$-CHUNK attributes END

//-$$$-CHUNK main-code-vertex BEGIN
vVisibility = aVisibility;
//-$$$-CHUNK main-code-vertex END

// --------------------------------------------------------------------------

//-$$$-CHUNK uniforms-fragment BEGIN
uniform vec4 uColor;
uniform float uRadius;
const float THICKNESS = 0.3;
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
    if (vVisibility > (1.0 - THICKNESS / uRadius)) {
        gl_FragColor = uColor;
    } else {
        discard;
    }
//-$$$-CHUNK main-code-fragment END
