#version 450 core

#include <scene.glsl>
#include <direct_lighting.glsl>

#define MAX_BOUNCES 	1
#define PI          	3.14159265359
#define SQRT2 			1.41421356237

layout(location = 0) out vec4 colorOutput;

layout(binding = 0, location = 0) uniform sampler2D positionDepthTex;
layout(binding = 1, location = 1) uniform sampler2D normalMaterialTex;

layout(binding = 2, location = 2) uniform sampler2D oldPositionDepthTex;
layout(binding = 3, location = 3) uniform sampler2D oldNormalMaterialTex;

layout(binding = 4, location = 4) uniform sampler2D oldColorTex;

layout(location = 5) uniform mat4 VPMatrix;
layout(location = 6) uniform int iFrame;

in vec2 uv;

float nrand(in vec2 n) {
	return fract(sin(dot(n.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 indirectLight(
	in vec3 pos, 
	in vec3 normal, 
	in vec3 albedo,
	in vec2 seed
) {
	vec3 accumulatedColor = vec3(0);
	vec3 colorMask        = vec3(1);	
	
	colorMask = albedo;
	
	pos += normal * EPSILON;
	for (int b = 0; b < MAX_BOUNCES; b++) {
		// Generate random dir vector
		vec3 rs     = pos + seed.x;
		vec3 random = vec3(nrand(rs.xy), nrand(rs.yz), nrand(rs.zx)) * 2.0 - 1.0;
		vec3 dir    = normalize(normal * SQRT2 + random);
		
		// Trace ray
		HitInfo hit = intersectScene(pos, dir, MAX_DISTANCE);
		if (hit.intersect) {
			// Accumulate color
			colorMask        *= materials[hit.materialID].albedo;
			accumulatedColor += colorMask * directLight(hit.position, hit.normal); // * min(1.0, 1.0 / hit.depth);
		
			// Update next ray position
			pos = hit.position + hit.normal * EPSILON; // Avoid self intersection
		} else {
			const vec3 sunColor = vec3(0.619095, 0.562118, 0.548401);
			colorMask        *= sunColor;
			accumulatedColor += colorMask * sunColor;
			
			break; // No more geometry, exit path
		}
	}
	
	return accumulatedColor;
}

vec2 reproject(in vec3 pos) {
	vec4 ptransform = VPMatrix * vec4(pos, 1.0);
	ptransform /= ptransform.w;
	return (vec2(ptransform.xy) + 1.0) * 0.5;
}

void main() {
	// Get GBuffer information
	const int materialID = int(texture(normalMaterialTex, uv).w);
	const vec3 normal    = texture(normalMaterialTex, uv).xyz;
	const vec3 position  = texture(positionDepthTex, uv).xyz;
	const float depth    = texture(positionDepthTex, uv).w;
	
	// Compute seed
	const vec2 seed = uv + iFrame;
	
	// Compute indirect light
	vec3 color = vec3(0);
	if (depth < MAX_DISTANCE) {
		const vec3 GI = indirectLight(position, normal, materials[materialID].albedo, seed);
		color = GI;
	}
	
	// Get old color
	const vec3 oldColor = texture(oldColorTex, uv).xyz;
	
	// Write color
	colorOutput = vec4(color, 1.0);
}