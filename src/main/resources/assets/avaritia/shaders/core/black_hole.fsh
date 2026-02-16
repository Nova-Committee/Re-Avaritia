#version 150

// Black Hole Fragment Shader
// Renders the gravitational lensing effect, event horizon, and accretion disk

uniform float GameTime;
uniform vec2 ScreenSize;
uniform float Radius;        // Black hole radius
uniform float Mass;          // Black hole mass (affects distortion strength)

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec2 texCoord2;
in vec3 normal;

out vec4 fragColor;

// Constants
const float PI = 3.14159265359;
const float EVENT_HORIZON_FACTOR = 0.15;
const float ACCRETION_DISK_INNER = 0.16;
const float ACCRETION_DISK_OUTER = 0.45;
const float GRAVITATIONAL_LENS_STRENGTH = 0.08;

// Hash function for procedural noise
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Simple noise function for accretion disk texture
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Fractal Brownian Motion for more complex noise
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p * frequency);
        amplitude *= 0.5;
        frequency *= 2.0;
    }

    return value;
}

// Calculate gravitational lensing distortion
vec2 gravitationalLens(vec2 uv, vec2 center, float strength) {
    vec2 dir = uv - center;
    float dist = length(dir);

    if (dist < 0.001) {
        return uv;
    }

    // Gravitational lensing formula (simplified Schwarzschild metric)
    // Light bends more as it gets closer to the event horizon
    float distortion = strength / (dist * dist + 0.01);
    distortion = min(distortion, 0.5);  // Limit maximum distortion

    // Direction of distortion (toward the black hole center)
    vec2 offset = -normalize(dir) * distortion;

    return uv + offset;
}

// Color palette for accretion disk (hot gas)
vec3 accretionDiskColor(float temperature, float noiseVal) {
    // Temperature gradient from hot inner to cooler outer
    vec3 hotColor = vec3(1.0, 0.95, 0.8);      // White-hot center
    vec3 warmColor = vec3(1.0, 0.6, 0.2);       // Orange
    vec3 coolColor = vec3(0.8, 0.2, 0.1);       // Dark red
    vec3 coolOuter = vec3(0.3, 0.05, 0.02);     // Dark red-black outer

    vec3 color;
    if (temperature > 0.7) {
        color = mix(warmColor, hotColor, (temperature - 0.7) / 0.3);
    } else if (temperature > 0.3) {
        color = mix(coolColor, warmColor, (temperature - 0.3) / 0.4);
    } else {
        color = mix(coolOuter, coolColor, temperature / 0.3);
    }

    // Add some variation from noise
    color *= (0.7 + 0.3 * noiseVal);

    return color;
}

void main() {
    // Center the UV coordinates (0,0 is center of the quad)
    vec2 uv = texCoord0 - 0.5;
    float dist = length(uv);
    float angle = atan(uv.y, uv.x);

    // Calculate event horizon radius
    float eventHorizon = EVENT_HORIZON_FACTOR * Radius;

    // ==================== EVENT HORIZON ====================
    // The region where nothing can escape - completely black
    if (dist < eventHorizon) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    // ==================== ACCRETION DISK ====================
    // Swirling ring of superheated matter around the black hole
    if (dist > ACCRETION_DISK_INNER && dist < ACCRETION_DISK_OUTER) {
        // Calculate rotation for the swirling effect
        // Inner parts rotate faster (like real black holes)
        float rotationSpeed = 3.0 / (dist + 0.1);
        float rotation = angle + GameTime * rotationSpeed;

        // Create polar coordinates for the disk texture
        vec2 polarUV = vec2(rotation / (2.0 * PI), dist * 4.0);

        // Sample the noise texture
        float noiseVal = fbm(polarUV * 8.0);

        // Calculate temperature based on distance (hotter closer to center)
        float temperature = 1.0 - smoothstep(ACCRETION_DISK_INNER, ACCRETION_DISK_OUTER, dist);

        // Get base color from temperature
        vec3 diskColor = accretionDiskColor(temperature, noiseVal);

        // Add Doppler beaming effect (one side brighter than the other)
        float doppler = 0.5 + 0.5 * sin(angle + PI * 0.5);
        diskColor *= (0.7 + 0.3 * doppler);

        // Calculate alpha (fade out at edges)
        float innerFade = smoothstep(ACCRETION_DISK_INNER, ACCRETION_DISK_INNER + 0.03, dist);
        float outerFade = 1.0 - smoothstep(ACCRETION_DISK_OUTER - 0.05, ACCRETION_DISK_OUTER, dist);
        float alpha = innerFade * outerFade;

        // Add some brightness variation
        alpha *= (0.8 + 0.4 * noiseVal);

        // Boost emission for glow effect
        diskColor *= 1.5;

        fragColor = vec4(diskColor, alpha);
        return;
    }

    // ==================== GRAVITATIONAL LENSING REGION ====================
    // The area just outside the event horizon where light bends
    if (dist > eventHorizon && dist < ACCRETION_DISK_INNER) {
        // Add a subtle glow near the event horizon
        float glowIntensity = 1.0 - smoothstep(eventHorizon, ACCRETION_DISK_INNER, dist);
        vec3 glowColor = vec3(1.0, 0.3, 0.05) * glowIntensity * 0.5;

        // Photon sphere ring - intense light bending region
        float photonSphere = smoothstep(eventHorizon + 0.02, eventHorizon + 0.03, dist) -
                           smoothstep(eventHorizon + 0.03, eventHorizon + 0.05, dist);
        glowColor += vec3(0.5, 0.2, 0.0) * photonSphere * 2.0;

        fragColor = vec4(glowColor, glowIntensity * 0.8);
        return;
    }

    // Outside the black hole effect area - make it mostly transparent
    if (dist > ACCRETION_DISK_OUTER) {
        // Add a subtle outer glow
        float outerGlow = 1.0 - smoothstep(ACCRETION_DISK_OUTER, 0.5, dist);
        vec3 glowColor = vec3(0.4, 0.1, 0.02) * outerGlow * 0.3;
        fragColor = vec4(glowColor, outerGlow * 0.2);
        return;
    }

    // Fallback - should not reach here
    discard;
}
