#version 450 core

#include <scene.glsl>
#include <direct_lighting.glsl>

layout(location = 0) out vec4 outputColor;

layout(binding = 0, location = 0) uniform sampler2D positionDepthTex;
layout(binding = 1, location = 1) uniform sampler2D normalMaterialTex;

layout(binding = 2, location = 2) uniform sampler2D indirectLightTex;

in vec2 uv;

void main() {
	const vec4 positionDepthData  = texture(positionDepthTex, uv);
	const vec4 normalMaterialData = texture(normalMaterialTex, uv);
	
	if (positionDepthData.w < MAX_DISTANCE) {
		const vec3 GI        = texture(indirectLightTex, uv).xyz;
		const int materialID = int(normalMaterialData.w);
		
		// Sample position
		vec3 sunContribution = directLight(positionDepthData.xyz, normalMaterialData.xyz);
		vec3 color           = materials[materialID].albedo * sunContribution;
		color               += GI * materials[materialID].albedo;
		
		outputColor = vec4(color, 1.0);
	} else {
		// Render sky background
		const vec3 backgroundColor = vec3(1., 163., 236.) / vec3(500);
		vec3 color = backgroundColor + abs(1.0 - normalMaterialData.y) * 0.4;
		
		outputColor = vec4(color, 1.0);
	}
}