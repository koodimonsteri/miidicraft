#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 textureCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=3) in mat4 modelViewInstancedMatrix;

out vec2 outTextureCoord;

uniform mat4 modelViewNonInstancedMatrix;
uniform mat4 projectionMatrix;
uniform int isInstanced;

void main()
{
    vec4 initPos = vec4(0, 0, 0, 0);

    mat4 modelViewMatrix;
    if ( isInstanced > 0 ) {
        modelViewMatrix = modelViewInstancedMatrix;
    }
    else {
        modelViewMatrix = modelViewNonInstancedMatrix;
    }

    initPos = vec4(position, 1.0);

    gl_Position = projectionMatrix * modelViewMatrix * initPos;
    outTextureCoord = textureCoord;
}