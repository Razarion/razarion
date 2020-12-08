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
uniform vec4 uBgColor;
uniform float uProgress;
//-$$$-CHUNK uniforms-fragment END

//-$$$-CHUNK main-code-fragment BEGIN
   if(vVisibility < uProgress) {
       gl_FragColor = uColor;
   } else {
       gl_FragColor = uBgColor;
   }
//-$$$-CHUNK main-code-fragment END
