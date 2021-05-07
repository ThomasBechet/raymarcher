#version 450

//layout(location = 0) uniform vec2 resolution;

out vec2 uv;
//out vec4 posPos;

//#define FXAA_SUBPIX_SHIFT 0.25
//#define FXAA_SUBPIX_SHIFT 0.0

void main() {
    // Compute UV and position
    float x = float((gl_VertexID & 1) << 2);
    float y = float((gl_VertexID & 2) << 1);
    uv.x = x * 0.5;
    uv.y = y * 0.5;
    gl_Position = vec4(x - 1.0, y - 1.0, 0, 1);

    // Setup FXAA
    //posPos.xy = uv;
    //posPos.zw = uv - ((1.0 / resolution) * (0.5 + (FXAA_SUBPIX_SHIFT)));
}