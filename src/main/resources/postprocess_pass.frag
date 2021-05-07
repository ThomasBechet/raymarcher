#version 450

layout(location = 0) out vec3 fragColor;

layout(location = 0) uniform sampler2D image;

in vec2 uv;

/*#define FXAA_SPAN_MAX   8.0
#define FXAA_REDUCE_MUL 0.125
//#define FXAA_REDUCE_MIN (1.0 / 128.0)
#define FXAA_REDUCE_MIN 0.0

vec3 FxaaPixelShader(
    vec4 posPos, // Output of FxaaVertexShader interpolated across screen.
    sampler2D tex, // Input texture.
    vec2 rcpFrame // Constant {1.0/frameWidth, 1.0/frameHeight}.
)
{
    //---------------------------------------------------------
    vec3 rgbNW = texture2D(tex, posPos.zw).xyz;
    vec3 rgbNE = texture2D(tex, posPos.zw + ivec2(1, 0) * rcpFrame.xy).xyz;
    vec3 rgbSW = texture2D(tex, posPos.zw + ivec2(0, 1) * rcpFrame.xy).xyz;
    vec3 rgbSE = texture2D(tex, posPos.zw + ivec2(1, 1) * rcpFrame.xy).xyz;
    vec3 rgbM  = texture2D(tex, posPos.xy).xyz;
    //---------------------------------------------------------
    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);
    //---------------------------------------------------------
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
    //---------------------------------------------------------
    vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));
    //---------------------------------------------------------
    float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX), max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX), dir * rcpDirMin)) * rcpFrame.xy;
    //--------------------------------------------------------
    vec3 rgbA = (1.0/2.0) * (
        texture2D(tex, posPos.xy + dir * (1.0 / 3.0 - 0.5)).xyz +
        texture2D(tex, posPos.xy + dir * (2.0 / 3.0 - 0.5)).xyz);
    vec3 rgbB = rgbA * (1.0 / 2.0) + (1.0 / 4.0) * (
        texture2D(tex, posPos.xy + dir * (0.0 / 3.0 - 0.5)).xyz +
        texture2D(tex, posPos.xy + dir * (3.0 / 3.0 - 0.5)).xyz);
    float lumaB = dot(rgbB, luma);
    if((lumaB < lumaMin) || (lumaB > lumaMax)) return rgbA;
    return rgbB;
}*/

vec3 tonemapFilmic(vec3 x) {
	vec3 X = max(vec3(0.0), x - 0.004);
	vec3 result = (X * (6.2 * X + 0.5)) / (X * (6.2 * X + 1.7) + 0.06);
	return pow(result, vec3(2.2));
}

void main() {
    vec4 data = texture(image, uv).rgba;
    fragColor = data.rgb;
}