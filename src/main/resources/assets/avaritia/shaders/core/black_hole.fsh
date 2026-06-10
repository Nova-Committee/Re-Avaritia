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

float hash3(vec3 p) {
    p = fract(p * vec3(123.34, 456.21, 345.45));
    p += dot(p, p + 45.32);
    return fract((p.x + p.y) * p.z);
}

// 时间作为第三维参与噪声插值，避免二维噪声平移时产生明显闪烁。
float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);

    float n000 = hash3(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash3(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash3(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash3(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash3(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash3(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash3(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash3(i + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, u.x);
    float nx10 = mix(n010, n110, u.x);
    float nx01 = mix(n001, n101, u.x);
    float nx11 = mix(n011, n111, u.x);
    float nxy0 = mix(nx00, nx10, u.y);
    float nxy1 = mix(nx01, nx11, u.y);
    return mix(nxy0, nxy1, u.z);
}

void main(void) {
    vec2 uv = texCoord0 * 2.0 - 1.0;
    uv.x *= 1.08;

    float mass = smoothstep(0.0, 1.0, clamp(absorbedMatter, 0.0, 1.0));
    float evap = clamp(evaporation, 0.0, 1.0);
    float survival = 1.0 - smoothstep(0.78, 1.0, evap);
    vec4 legacyMask = texture(Sampler0, texCoord0);
    float textureFeather = mix(0.82, 1.0, legacyMask.r);
    float r = length(uv);
    float angle = atan(uv.y, uv.x);
    float spin = time * (0.018 + mass * 0.014);
    float edgeFeather = max(fwidth(r) * 1.5, 0.018);

    float horizonRadius = 0.175 + mass * 0.07;
    float horizon = 1.0 - smoothstep(horizonRadius, horizonRadius + edgeFeather, r);
    float photonWidth = max(0.026, edgeFeather * 1.25);
    float photonRing = exp(-pow((r - horizonRadius * 1.32) / photonWidth, 2.0));
    float outerLens = exp(-pow((r - horizonRadius * 2.35) / 0.13, 2.0));

    float diskWarp = 0.062 * sin(angle * 2.0 + spin * 1.35) + 0.022 * sin(angle * 4.0 - spin * 0.8);
    float diskY = uv.y + diskWarp * (1.0 - smoothstep(0.2, 1.0, r));
    float diskCore = exp(-abs(diskY) / (0.064 + mass * 0.026));
    float diskMask = smoothstep(horizonRadius * 0.78, horizonRadius * 1.95, abs(uv.x))
            * (1.0 - smoothstep(0.92, 1.14, r));
    float diskNoise = noise3(vec3(angle * 2.3 + spin * 1.8, r * 8.0, time * 0.032));
    diskNoise = smoothstep(0.12, 0.94, diskNoise);
    float disk = diskCore * diskMask * (0.68 + diskNoise * 0.34) * (0.72 + mass * 0.65);

    float blueShift = smoothstep(-0.88, 0.76, uv.x);
    vec3 coldEdge = vec3(0.25, 0.54, 1.0);
    vec3 hotCore = vec3(1.0, 0.78, 0.28);
    vec3 redTail = vec3(1.0, 0.26, 0.06);
    vec3 diskColor = mix(redTail, hotCore, blueShift);
    diskColor = mix(diskColor, coldEdge, smoothstep(0.45, 1.0, blueShift) * 0.38);

    float lensPulse = 0.88 + 0.12 * sin(time * 0.055 + angle * 4.0);
    vec3 color = vec3(0.0);
    color += diskColor * disk;
    color += vec3(1.0, 0.88, 0.52) * photonRing * (1.25 + mass * 0.7);
    color += vec3(0.32, 0.58, 1.0) * outerLens * lensPulse * (0.28 + mass * 0.28);

    float jetColumn = exp(-abs(uv.x) / 0.07) * smoothstep(0.16, 0.88, abs(uv.y)) * (1.0 - smoothstep(0.86, 1.18, abs(uv.y)));
    float jetSpark = noise3(vec3(uv.x * 10.0, uv.y * 7.0, time * 0.075 + mass * 0.4));
    jetSpark = smoothstep(0.2, 0.95, jetSpark);
    float evaporationFlash = sin(evap * M_PI);
    float jet = jetColumn * evaporationFlash * (0.72 + jetSpark * 0.38);
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
