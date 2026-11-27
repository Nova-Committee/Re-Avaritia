#version 150

// Black Hole Vertex Shader
// Transforms vertices for the black hole entity billboard

in vec4 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in vec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 CHUNK_OFFSET;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec2 texCoord2;
out vec3 normal;

void main() {
    // Apply chunk offset for rendering
    vec4 worldPosition = Position;
    worldPosition.xyz += CHUNK_OFFSET;

    // Calculate final position
    gl_Position = ProjMat * ModelViewMat * worldPosition;

    // Pass data to fragment shader
    vertexColor = Color;
    texCoord0 = UV0;
    texCoord1 = UV1;
    texCoord2 = UV2;
    normal = Normal;
}
