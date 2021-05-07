#version 450 core

#include <scene.glsl>

layout(location = 0) out vec4 positionDepthTex;
layout(location = 1) out vec4 normalMaterialTex;

layout(location = 1) uniform vec3 eye;

in vec2 pos;
in mat4 invVPMatrix;

void main() {
	// Get direction vector
	vec4 ptransform = invVPMatrix * vec4(pos, 1, 1);
	vec3 dir = normalize((ptransform / ptransform.w).xyz - eye);
	
	// Primary ray
	HitInfo hit;
	hit = intersectScene(eye, dir, MAX_DISTANCE);
	if (hit.intersect) {		
		// Write images
		positionDepthTex  = vec4(hit.position, hit.depth);
		normalMaterialTex = vec4(hit.normal, hit.materialID); 
	} else {
		// Write images
		positionDepthTex  = vec4(0, 0, 0, MAX_DISTANCE);
		normalMaterialTex = vec4(dir, -1);
	}
}