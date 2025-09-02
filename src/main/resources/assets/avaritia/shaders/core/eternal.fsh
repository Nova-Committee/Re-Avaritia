#version 150

#define M_PI 3.1415926535897932384626433832795

#moj_import <fog.glsl>

const int cosmiccount = 40; // 改为40以匹配uniform大小
const int cosmicoutof = 101;
const float lightmix = 0.0f; // 完全去除光照混合，让粒子更纯粹

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform float time;

uniform float yaw;
uniform float pitch;
uniform float externalScale;

uniform float opacity;

uniform mat2 cosmicuvs[cosmiccount]; // 现在可以使用全部40个纹理

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
    0.0,                                0.0,                                0.0,                                1.0);
}

void main (void)
{
    vec4 mask = texture(Sampler0, texCoord0.xy);

    float oneOverExternalScale = 1.0/externalScale;

    int uvtiles = 128;


    vec4 col = vec4(0.05, 0.0, 0.15, 1.0);

    float pulse = mod(time, 800)/800.0;


    col.r = sin(pulse*M_PI*3 + 0.5) * 0.15 + 0.25;
    col.g = cos(pulse*M_PI*2 + 1.0) * 0.1 + 0.15;
    col.b = sin(pulse*M_PI*4) * 0.2 + 0.45;


    vec4 dir = normalize(vec4(-fPos, 0));


    float sb = sin(pitch);
    float cb = cos(pitch);
    dir = normalize(vec4(dir.x, dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb, 0));

    float sa = sin(-yaw);
    float ca = cos(-yaw);
    dir = normalize(vec4(dir.z * sa + dir.x * ca, dir.y, dir.z * ca - dir.x * sa, 0));

    vec4 ray;


    for (int i=0; i<128; i++) { // 使用128个粒子
        int mult = 128-i;


        int j = i + 13;
        float rand1 = (j * j * 5743 + j * 19) * 2.2F;
        int k = j + 5;
        float rand2 = (k * k * k * 389 + k * 47) * 3.2F;
        float rand3 = rand1 * 401.3 + rand2 * 83.7;


        vec3 axis = normalize(vec3(cos(rand1 * 0.7), sin(rand2 * 0.8) , cos(rand3 * 0.9)));


        ray = dir * rotationMatrix(axis, mod(rand3 * 2.0, 2*M_PI));


        float rawu = 0.5 + (atan(ray.z,ray.x)/(2*M_PI));
        float rawv = 0.5 + (asin(ray.y)/M_PI);


        float scale = mult*0.05 + 0.05;
        float u = rawu * scale * externalScale;
        float v = (rawv + time * 0.0005 * oneOverExternalScale) * scale * 0.2 * externalScale;

        vec2 tex = vec2( u, v );


        int tu = int(mod(floor(u*uvtiles),uvtiles));
        int tv = int(mod(floor(v*uvtiles),uvtiles));


        int position = ((197 * tu) + (571 * tv) + (409 * (i+23)) + 23149 ) ^ 23;
        int symbol = int(mod(position, cosmicoutof));
        int rotation = int(mod(pow(tv * 0.7,float(tu * 0.8)) + tv + 11 + tu*i, 16));
        bool flip = false;
        if (rotation >= 8) {
            rotation -= 8;
            flip = true;
        }


        // 添加聚集效果 - 只在中心区域显示粒子
        float centerDistance = length(vec2(rawu - 0.5, rawv - 0.5));
        if (centerDistance > 0.4) continue; // 只在中心40%区域内显示粒子


        if (symbol >= 0 && symbol < cosmicoutof) {
            vec2 cosmictex = vec2(1.0,1.0);
            vec4 tcol = vec4(1.0,0.0,0.0,1.0);


            float ru = clamp(mod(u,1.0)*uvtiles - tu, 0.0, 1.0);
            float rv = clamp(mod(v,1.0)*uvtiles - tv, 0.0, 1.0);

            if (flip) {
                ru = 1.0 - ru;
            }

            float oru = ru;
            float orv = rv;


            if (rotation == 1) {
                oru = 1.0-rv;
                orv = ru;
            } else if (rotation == 2) {
                oru = 1.0-ru;
                orv = 1.0-rv;
            } else if (rotation == 3) {
                oru = rv;
                orv = 1.0-ru;
            } else if (rotation == 4) {
                oru = 0.5 + (ru - 0.5) * cos(0.785) - (rv - 0.5) * sin(0.785);
                orv = 0.5 + (ru - 0.5) * sin(0.785) + (rv - 0.5) * cos(0.785);
            } else if (rotation == 5) {
                oru = 0.5 + (ru - 0.5) * cos(1.57) - (rv - 0.5) * sin(1.57);
                orv = 0.5 + (ru - 0.5) * sin(1.57) + (rv - 0.5) * cos(1.57);
            } else if (rotation == 6) {
                oru = 0.5 + (ru - 0.5) * cos(2.355) - (rv - 0.5) * sin(2.355);
                orv = 0.5 + (ru - 0.5) * sin(2.355) + (rv - 0.5) * cos(2.355);
            } else if (rotation == 7) {
                oru = ru * 0.8 + 0.1;
                orv = rv * 0.8 + 0.1;
            }


            float umin = cosmicuvs[symbol % cosmiccount][0][0];
            float umax = cosmicuvs[symbol % cosmiccount][1][0];
            float vmin = cosmicuvs[symbol % cosmiccount][0][1];
            float vmax = cosmicuvs[symbol % cosmiccount][1][1];


            cosmictex.x = umin * (1.0-oru) + umax * oru;
            cosmictex.y = vmin * (1.0-orv) + vmax * orv;

            tcol = texture(Sampler0, cosmictex);


            float a = tcol.r * (3.0 + (1.0/mult) * 3.0) * (1.0-smoothstep(0.001, 0.5, abs(rawv-0.5)));


            float r = (mod(rand1, 41.0)/41.0) * 0.8 + 0.2;
            float g = (mod(rand2, 31.0)/31.0) * 0.7 + 0.3;
            float b = (mod(rand3, 23.0)/23.0) * 0.8 + 0.2;


            float pulseEffect = 0.0 + sin(time*0.05 + rand1)*1.0;
            col = col + vec4(r*b,g*r,b*g,1)*a * pulseEffect;
        }
    }


    vec3 shade = vertexColor.rgb * (lightmix) + vec3(1.0-lightmix,1.0-lightmix,1.0-lightmix);
    col.rgb *= shade;


    col.a *= mask.r * opacity;

    col = clamp(col,0.0,1.0);

    fragColor = linear_fog(col * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
