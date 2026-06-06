package com.avaritia.client.render.mesh;

import com.avaritia.api.client.util.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleObjMeshLoader {
    private SimpleObjMeshLoader() {
    }

    public static Map<String, SimpleMesh> load(Identifier location) {
        return load(Minecraft.getInstance().getResourceManager(), location, false);
    }

    public static Map<String, SimpleMesh> load(Identifier location, boolean swapYZ) {
        return load(Minecraft.getInstance().getResourceManager(), location, swapYZ);
    }

    public static Map<String, SimpleMesh> load(ResourceProvider provider, Identifier location, boolean swapYZ) {
        return parseLines(ResourceUtils.loadResource(provider, location), swapYZ);
    }

    static Map<String, SimpleMesh> parseLines(List<String> lines, boolean swapYZ) {
        List<Vec3> positions = new ArrayList<>();
        List<Vec2> texCoords = new ArrayList<>();
        List<Vec3> normals = new ArrayList<>();
        Map<String, SimpleMesh> meshes = new LinkedHashMap<>();

        String currentName = "model";
        String pendingName = currentName;
        List<SimpleMesh.Vertex> currentVertices = null;

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex).replaceAll("\\s+", " ").trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(" ", 2);
            String key = parts[0];
            String value = parts.length > 1 ? parts[1] : "";
            switch (key) {
                case "v" -> positions.add(parseVec3(value, lineIndex, swapYZ));
                case "vt" -> texCoords.add(parseUv(value, lineIndex));
                case "vn" -> normals.add(parseVec3(value, lineIndex, swapYZ));
                case "f" -> {
                    if (currentVertices == null) {
                        currentVertices = new ArrayList<>();
                        pendingName = currentName;
                    }
                    appendFace(value, lineIndex, positions, texCoords, normals, currentVertices);
                }
                case "g", "o" -> {
                    if (currentVertices != null) {
                        putMesh(meshes, pendingName, new SimpleMesh(currentVertices));
                        currentVertices = null;
                    }
                    currentName = value.isBlank() ? "model" : value;
                    pendingName = currentName;
                }
                default -> {
                }
            }
        }

        if (currentVertices != null) {
            putMesh(meshes, pendingName, new SimpleMesh(currentVertices));
        }

        return meshes;
    }

    private static void appendFace(String value, int lineIndex, List<Vec3> positions, List<Vec2> texCoords,
                                   List<Vec3> normals, List<SimpleMesh.Vertex> output) {
        String[] refs = value.split(" ");
        if (refs.length != 4) {
            throw new IllegalStateException("Only quad OBJ faces are supported. Line " + (lineIndex + 1) + ": " + value);
        }

        int[] order = {0, 3, 2, 1};
        FaceVertex[] face = new FaceVertex[refs.length];
        for (int i = 0; i < refs.length; i++) {
            face[i] = parseFaceVertex(refs[i], lineIndex);
        }

        for (int index : order) {
            output.add(toVertex(face[index], lineIndex, positions, texCoords, normals));
        }
    }

    private static FaceVertex parseFaceVertex(String value, int lineIndex) {
        String[] parts = value.split("/", -1);
        if (parts.length == 0 || parts[0].isBlank()) {
            throw new IllegalStateException("OBJ face vertex is missing a position. Line " + (lineIndex + 1));
        }

        int position = Integer.parseInt(parts[0]);
        int texCoord = parts.length > 1 && !parts[1].isBlank() ? Integer.parseInt(parts[1]) : 0;
        int normal = parts.length > 2 && !parts[2].isBlank() ? Integer.parseInt(parts[2]) : 0;
        return new FaceVertex(position, texCoord, normal);
    }

    private static SimpleMesh.Vertex toVertex(FaceVertex face, int lineIndex, List<Vec3> positions, List<Vec2> texCoords, List<Vec3> normals) {
        Vec3 position = positions.get(resolveIndex(face.position(), positions.size(), lineIndex));
        Vec2 texCoord = face.texCoord() == 0 ? Vec2.ZERO : texCoords.get(resolveIndex(face.texCoord(), texCoords.size(), lineIndex));
        Vec3 normal = face.normal() == 0 ? Vec3.UP : normals.get(resolveIndex(face.normal(), normals.size(), lineIndex));
        return new SimpleMesh.Vertex(position.x(), position.y(), position.z(), texCoord.u(), texCoord.v(), normal.x(), normal.y(), normal.z());
    }

    private static int resolveIndex(int index, int size, int lineIndex) {
        int resolved = index > 0 ? index - 1 : size + index;
        if (resolved < 0 || resolved >= size) {
            throw new IllegalStateException("OBJ index out of bounds. Line " + (lineIndex + 1) + ": " + index);
        }
        return resolved;
    }

    private static Vec3 parseVec3(String value, int lineIndex, boolean swapYZ) {
        String[] parts = value.split(" ");
        if (parts.length < 3) {
            throw new IllegalStateException("Expected x, y and z components. Line " + (lineIndex + 1) + ": " + value);
        }

        float x = Float.parseFloat(parts[0]);
        float y = Float.parseFloat(parts[1]);
        float z = Float.parseFloat(parts[2]);
        return swapYZ ? new Vec3(x, z, y) : new Vec3(x, y, z);
    }

    private static Vec2 parseUv(String value, int lineIndex) {
        String[] parts = value.split(" ");
        if (parts.length < 2) {
            throw new IllegalStateException("Expected u and v components. Line " + (lineIndex + 1) + ": " + value);
        }

        return new Vec2(Float.parseFloat(parts[0]), 1.0F - Float.parseFloat(parts[1]));
    }

    private static void putMesh(Map<String, SimpleMesh> meshes, String name, SimpleMesh mesh) {
        String originalName = name == null || name.isBlank() ? "model" : name;
        String nextName = originalName;
        int index = 0;
        while (meshes.containsKey(nextName)) {
            nextName = originalName + index;
            index++;
        }
        meshes.put(nextName, mesh);
    }

    private record FaceVertex(int position, int texCoord, int normal) {
    }

    private record Vec2(float u, float v) {
        private static final Vec2 ZERO = new Vec2(0.0F, 0.0F);
    }

    private record Vec3(float x, float y, float z) {
        private static final Vec3 UP = new Vec3(0.0F, 1.0F, 0.0F);
    }
}
