package com.btxtech.client.renderer.engine;

/**
 * Created by Beat
 * 03.09.2015.
 */
public interface Renderer {
    // Attributes
    String A_VERTEX_POSITION = "aVertexPosition";
    String A_VERTEX_NORMAL = "aVertexNormal";
    String A_TEXTURE_COORDINATE = "aTextureCoord";
    String A_BARYCENTRIC = "aBarycentric";
    // Uniform model matrix
    String U_PERSPECTIVE_MATRIX = "uPMatrix";
    String U_VIEW_MATRIX = "uVMatrix";
    String U_VIEW_NORM_MATRIX = "uNVMatrix";
    String U_MODEL_MATRIX = "uMMatrix";
    String U_MODEL_NORM_MATRIX = "uNMatrix";
    // Uniform Light
    String U_LIGHT_DIRECTION = "uLightDirection";
    String U_LIGHT_DIFFUSE= "uLightDiffuse";
    String U_LIGHT_AMBIENT = "uLightAmbient";
    String U_LIGHT_SPECULAR_INTENSITY = "uLightSpecularIntensity";
    String U_LIGHT_SPECULAR_HARDNESS = "uLightSpecularHardness";

    void setId(int id);

    void setupImages();

    void fillBuffers();

    void draw();

    boolean hasElements();
}
