#version 330
#extension GL_ARB_separate_shader_objects : require

// 26.3 RenderPearl: includes use the minecraft: namespace, transforms/fog live in std140 blocks
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

// ENTITY vertex format
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;

uniform sampler2D Sampler2;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec2 texCoord0;
layout(location = 4) out vec3 viewPos;

void main() {
    vec4 viewPos4 = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos4;
    viewPos = viewPos4.xyz;

    sphericalVertexDistance = fog_spherical_distance(viewPos4.xyz);
    cylindricalVertexDistance = fog_cylindrical_distance(viewPos4.xyz);

    vertexColor = Color * sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
}
