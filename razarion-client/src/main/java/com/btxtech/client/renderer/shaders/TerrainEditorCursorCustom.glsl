//-$$$-CHUNK uniforms-fragment BEGIN
uniform int uCursorType;
//-$$$-CHUNK uniforms-fragment END


//-$$$-CHUNK main-code-fragment BEGIN
    if(uCursorType == 0) {
        // CREATE
        gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
    } else if(uCursorType == 1) {
        // MODIFY
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
    } else  if(uCursorType == 2){
        // REMOVE_MODE
        gl_FragColor = vec4(1.0, 0.8, 0.8, 1.0);
     } else {
         gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
   }
//-$$$-CHUNK main-code-fragment END
