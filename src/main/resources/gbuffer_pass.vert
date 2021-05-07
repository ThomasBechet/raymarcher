#version 450 core

layout(location = 0) uniform mat4 VPMatrix;

out vec2 pos;
flat out mat4 invVPMatrix;

void main() {
	float x = float((gl_VertexID & 1) << 2);
	float y = float((gl_VertexID & 2) << 1);
	
	pos = (vec2(x, y) * 0.5) * 2.0 - 1.0;
	invVPMatrix = inverse(VPMatrix);
	
	gl_Position = vec4(x - 1.0, y - 1.0, 0.0, 1.0);
}