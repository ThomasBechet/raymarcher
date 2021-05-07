#define MAX_RAYMARCH_STEP  512
#define MAX_DISTANCE       300
#define MIN_HIT_DISTANCE   0.0001
#define MAX_SHAPE_COUNT    128
#define MAX_MATERIAL_COUNT 64
#define MAX_INSTANCE_COUNT 128
#define EPSILON            0.005

/* ------------- Structures --------------- */
struct Material {
    vec3 albedo;	 // 12 0
    float roughness; // 4  12
	float metallic;  // 4  16
	int _pad0;		 // 4  20
	int _pad1;		 // 4  24
	int _pad2;		 // 4  28
	// total 32
};

struct Shape {
    int type;       // 4  0
    int materialID; // 4  4
    int _pad0;      // 4  8
    int _pad1;      // 4  12
    vec3 position;  // 12 16
    vec4 f0;        // 16 32
    vec4 f1;        // 16 48
	// total 52
};

/* ------------- UBO ---------------------- */
layout(binding = 0, std140) uniform LayoutHeader {
    int materialCount;
    int shapeCount;
};

layout(binding = 1, std140) uniform Materials {
    Material materials[MAX_MATERIAL_COUNT];
};

layout(binding = 2, std140) uniform Shapes {
    Shape shapes[MAX_SHAPE_COUNT];
};

/* ------------ SHAPES SDF --------- */

#SHAPE_SD_FUNCTIONS_TOKEN

/* ----------- SCENE -------------- */

#MAP_SD_FUNCTION_TOKEN

float map(in vec3 p) {
    float sd = MAX_DISTANCE;
    for (int i = 0; i < shapeCount; i++) {
        sd = min(sd, sdShape(p - shapes[i].position.xyz, shapes[i]));
    }
    return sd;
}
float mapShape(in vec3 p, out int shapeID) {
    float sd = MAX_DISTANCE;
    float s;
    for (int i = 0; i < shapeCount; i++) {
        if ((s = sdShape(p - shapes[i].position.xyz, shapes[i])) < sd) {
            sd = s;
            shapeID = i;
        }
    }
    return sd;
}

vec3 normalScene(in vec3 p, in float sd) {
#if 1
    const vec2 e = vec2(EPSILON, 0);
    return normalize(vec3(
        sd - map(vec3(p - e.xyy)),
        sd - map(vec3(p - e.yxy)),
        sd - map(vec3(p - e.yyx))
    ));
#else
    vec3 n = vec3(0.0);
    for(int i = 0; i < 4; i++) {
        vec3 e = 0.5773 * (2.0 * vec3((((i + 3) >> 1) & 1),((i >> 1) & 1),(i & 1)) - 1.0);
        n += e * map(p + 0.0005 * e).x;
    }
    return normalize(n);
#endif
}

struct HitInfo {
	vec3 position;   // Relevant if intersect
	int materialID;  // Relevant if intersect
	vec3 normal;     // Relevant if intersect
	int shapeID;	 // Relevant if intersect
	float depth;     // Relevant if intersect or maxDepth instead
	bool intersect;
};

HitInfo intersectScene(in vec3 p, in vec3 dir, in float maxDepth) {
	HitInfo hit;
	
	hit.intersect = false;
	hit.depth     = EPSILON;
	
	float sd;
	for (int step = 0; step < MAX_RAYMARCH_STEP; step++) {
		hit.position = p + hit.depth * dir;
		sd = mapShape(hit.position, hit.shapeID);
		if (sd < MIN_HIT_DISTANCE) {
			hit.intersect = true;
			break;
		}
		hit.depth += sd;
		if (hit.depth > maxDepth) {
			hit.depth = maxDepth;
			break;
		}
	}
	
	if (hit.intersect) {
		hit.normal     = normalScene(hit.position, sd);
		hit.materialID = shapes[hit.shapeID].materialID;
	}
	
	return hit;
}