#version 330

#define M_PI 3.1415926535897932384626433832795

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

const int cosmiccount = 10;

uniform sampler2D Sampler0;

layout(std140) uniform AvaritiaCosmic {
    vec4 CosmicParams0;
    vec4 CosmicParams1;
    vec4 CosmicUvs[cosmiccount];
};

#define time CosmicParams0.x
#define yaw CosmicParams0.y
#define pitch CosmicParams0.z
#define absorbedMatter CosmicParams0.w
#define evaporation CosmicParams1.x

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

void main(void) {
    vec2 uv = texCoord0 * 2.0 - 1.0;
    uv.x *= 1.08;

    float mass = clamp(absorbedMatter, 0.0, 1.0);
    float evap = clamp(evaporation, 0.0, 1.0);
    float survival = 1.0 - smoothstep(0.78, 1.0, evap);
    vec4 legacyMask = texture(Sampler0, texCoord0);
    float textureFeather = mix(0.82, 1.0, legacyMask.r);
    float r = length(uv);
    float angle = atan(uv.y, uv.x);
    float spin = time * (0.026 + mass * 0.018);

    float horizonRadius = 0.175 + mass * 0.07;
    float horizon = 1.0 - smoothstep(horizonRadius, horizonRadius + 0.018, r);
    float photonRing = exp(-pow((r - horizonRadius * 1.32) / 0.024, 2.0));
    float outerLens = exp(-pow((r - horizonRadius * 2.35) / 0.11, 2.0));

    float diskWarp = 0.08 * sin(angle * 2.0 + spin * 1.7) + 0.035 * sin(angle * 5.0 - spin);
    float diskY = uv.y + diskWarp * (1.0 - smoothstep(0.2, 1.0, r));
    float diskCore = exp(-abs(diskY) / (0.058 + mass * 0.025));
    float diskMask = smoothstep(horizonRadius * 0.78, horizonRadius * 1.95, abs(uv.x))
            * (1.0 - smoothstep(0.92, 1.14, r));
    float diskNoise = noise(vec2(angle * 4.0 + spin * 5.0, r * 13.0 - spin * 2.0));
    float disk = diskCore * diskMask * (0.58 + diskNoise * 0.52) * (0.72 + mass * 0.65);

    float blueShift = smoothstep(-0.88, 0.76, uv.x);
    vec3 coldEdge = vec3(0.25, 0.54, 1.0);
    vec3 hotCore = vec3(1.0, 0.78, 0.28);
    vec3 redTail = vec3(1.0, 0.26, 0.06);
    vec3 diskColor = mix(redTail, hotCore, blueShift);
    diskColor = mix(diskColor, coldEdge, smoothstep(0.45, 1.0, blueShift) * 0.38);

    float lensPulse = 0.82 + 0.18 * sin(time * 0.09 + angle * 6.0);
    vec3 color = vec3(0.0);
    color += diskColor * disk;
    color += vec3(1.0, 0.88, 0.52) * photonRing * (1.25 + mass * 0.7);
    color += vec3(0.32, 0.58, 1.0) * outerLens * lensPulse * (0.28 + mass * 0.28);

    float jetColumn = exp(-abs(uv.x) / 0.07) * smoothstep(0.16, 0.88, abs(uv.y)) * (1.0 - smoothstep(0.86, 1.18, abs(uv.y)));
    float jetSpark = noise(vec2(uv.x * 18.0 + spin * 2.0, uv.y * 11.0 - spin * 7.0));
    float evaporationFlash = sin(evap * M_PI);
    float jet = jetColumn * evaporationFlash * (0.65 + jetSpark * 0.55);
    color += vec3(0.45, 0.78, 1.0) * jet * 2.4;
    color += vec3(1.0, 0.95, 0.82) * evaporationFlash * exp(-pow((r - horizonRadius * 1.6) / 0.19, 2.0));

    float alpha = max(disk * 0.9, photonRing * 0.95);
    alpha = max(alpha, outerLens * 0.36);
    alpha = max(alpha, jet * 0.75);
    alpha *= (1.0 - smoothstep(1.02, 1.18, r));
    alpha = max(alpha, horizon * survival);
    alpha *= survival * textureFeather;

    color = mix(color, vec3(0.0), horizon);
    color += legacyMask.rgb * 0.012 * (1.0 - horizon);
    color *= vertexColor.rgb;

    vec4 col = vec4(clamp(color, 0.0, 5.0), clamp(alpha, 0.0, 1.0));
    fragColor = apply_fog(col * ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
