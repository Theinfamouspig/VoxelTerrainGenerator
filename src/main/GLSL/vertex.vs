#version 450

layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 inColour;
layout (location = 2) in vec3 inNormal;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;

out vec3 exColour;
out vec3 vertexPos;
out vec3 vertexNormal;

void main()
{
    exColour = inColour;
    vertexNormal = inNormal;
    vertexPos = pos;
    gl_Position = projection * view * model * vec4(pos, 1.0);
}