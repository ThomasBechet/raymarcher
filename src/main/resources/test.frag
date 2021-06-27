#version 450 core

layout(location = 0) out vec4 color;

//layout(push_constant) uniform Constants {
//    mat4 VPMatrix;
//    vec3 eye;
//};

//layout(location = 1) in vec2 pos;
//layout(location = 2) in flat mat4 invVPMatrix;

void main() {
    color = vec4(0.1, 0, 0, 1);
}