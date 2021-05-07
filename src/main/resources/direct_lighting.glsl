/* ------------- DIRECT LIGHT --------------- */

float hardShadow(in vec3 p, in vec3 ldir, in float mint, in float maxt) {
    for (float t = mint; t < maxt;) {
        float sd = map(p + ldir * t);
        if (sd < MIN_HIT_DISTANCE) return 0.0f;
        t += sd;
    }
    return 1.0;
}
float softShadow(in vec3 p, in vec3 ldir, in float mint, in float maxt, in float k) {
    float res = 1.0f;
    float ph = 1e20f;
    for (float t = mint; t < maxt;) {
        float sd = map(p + ldir * t);
        if (sd < MIN_HIT_DISTANCE) return 0.0f;
        float y = sd * sd / (2.0f * ph);
        float d = sqrt(sd * sd - y * y);
        res = min(res, k * d / max(0.0f, t - y));
        ph = sd;
        t += sd;
    }
    return res;
}

vec3 directLight(in vec3 pos, in vec3 normal) {
	// Compute direct lighting
	const vec3 sunDir   = normalize(vec3(-0.5, 1.0, 0.5));
	const vec3 sunColor = vec3(0.619095, 0.562118, 0.548401);
	const vec3 skyColor = vec3(1);
	
	// Sun color
	vec3 color = vec3(0);
	float sunDot = max(0, dot(sunDir, normal));
	float sunShadow = 1.0f;
	if (sunDot > 0.0f) {
		//sunShadow = softShadow(pos + normal * EPSILON, sunDir, EPSILON, MAX_DISTANCE, 32.0);
		sunShadow = hardShadow(pos + normal * EPSILON, sunDir, EPSILON, MAX_DISTANCE);
	}
	color += sunDot * sunShadow * sunColor * vec3(2.0);
	
	return color;
}