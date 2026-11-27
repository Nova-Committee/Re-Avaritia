package committee.nova.mods.avaritia.client.model.loader;

import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.api.client.render.CCModel;
import committee.nova.mods.avaritia.api.client.render.model.OBJParser;
import committee.nova.mods.avaritia.api.util.vec.Vector3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NativeObjModelLoaderTest {
    @Test
    void allNonNormalShieldFacesHaveOutwardWindingMatchingAuthoredNormals() {
        ResourceProvider resources = location -> {
            var url = getClass().getResource("/assets/" + location.getNamespace() + "/" + location.getPath());
            return url == null ? Optional.empty() : Optional.of(new Resource(null, url::openStream));
        };
        for (String mode : new String[]{"defending", "definite_defending", "float"}) {
            var parts = OBJParser.parse(resources,
                    new ResourceLocation("avaritia", "models/obj/infinity_shield_" + mode + ".obj"),
                    VertexFormat.Mode.QUADS, null, false);
            assertFalse(parts.isEmpty(), mode);
            parts.forEach((name, part) -> {
                assertNotNull(part.normals(), mode + "/" + name);
                NativeObjModelLoader.preparePart(part);
                for (int face = 0; face < part.verts.length; face += 4) {
                    Vector3 outward = geometricNormal(part, face);
                    for (int vertex = 0; vertex < 4; vertex++) {
                        assertTrue(outward.dotProduct(part.normals()[face + vertex]) > 0.99,
                                mode + "/" + name + " face " + face / 4 + " has inside-out winding");
                    }
                }
            });
        }
    }

    @Test
    void restoredWindingKeepsUvAndAuthoredNormalsAttachedToTheirVertices() {
        CCModel part = parse("""
                v 0 0 0
                v 1 0 0
                v 1 1 0
                v 0 1 0
                vt 0.1 0.2
                vt 0.3 0.4
                vt 0.5 0.6
                vt 0.7 0.8
                vn 0 0 1
                vn 0.1 0 0.9
                vn 0 0.1 0.9
                vn -0.1 0 0.9
                o quad
                f 1/1/1 2/2/2 3/3/3 4/4/4
                """);
        // The shared CCModel parser retains its own reversed order; only the native
        // baked-model adapter restores OBJ order, leaving other OBJ consumers alone.
        assertEquals(0, part.verts[1].vec.x);
        assertEquals(1, part.verts[1].vec.y);
        NativeObjModelLoader.preparePart(part);
        double[][] expected = {{0, 0, 0.1, 0.8, 0, 0}, {1, 0, 0.3, 0.6, 0.1, 0},
                {1, 1, 0.5, 0.4, 0, 0.1}, {0, 1, 0.7, 0.2, -0.1, 0}};
        for (int i = 0; i < 4; i++) {
            assertEquals(expected[i][0], part.verts[i].vec.x, 1e-9);
            assertEquals(expected[i][1], part.verts[i].vec.y, 1e-9);
            assertEquals(expected[i][2], part.verts[i].uv.u, 1e-9);
            assertEquals(expected[i][3], part.verts[i].uv.v, 1e-9);
            assertEquals(expected[i][4], part.normals()[i].x, 1e-9);
            assertEquals(expected[i][5], part.normals()[i].y, 1e-9);
            assertEquals(i == 0 ? 1 : 0.9, part.normals()[i].z, 1e-9);
        }
    }

    @Test
    void missingNormalsAreComputedAfterRestoringQuadAndTriangleWinding() {
        for (String face : new String[]{"1 2 3 4", "1 2 3"}) {
            CCModel part = parse("""
                    v 0 0 0
                    v 1 0 0
                    v 1 1 0
                    v 0 1 0
                    o polygon
                    """ + "f " + face + "\n");
            assertNull(part.normals());
            NativeObjModelLoader.preparePart(part);
            assertEquals(1, geometricNormal(part, 0).z, 1e-9);
            for (Vector3 normal : part.normals()) {
                assertEquals(0, normal.x, 1e-9);
                assertEquals(0, normal.y, 1e-9);
                assertEquals(1, normal.z, 1e-9);
            }
        }
    }

    private static Vector3 geometricNormal(CCModel part, int face) {
        Vector3 origin = part.verts[face].vec;
        return part.verts[face + 1].vec.copy().subtract(origin)
                .crossProduct(part.verts[face + 3].vec.copy().subtract(origin)).normalize();
    }

    private static CCModel parse(String obj) {
        ResourceProvider resources = location -> Optional.of(new Resource(null,
                () -> new ByteArrayInputStream(obj.getBytes(StandardCharsets.UTF_8))));
        return OBJParser.parse(resources, new ResourceLocation("avaritia", "test.obj"),
                VertexFormat.Mode.QUADS, null, true).values().iterator().next();
    }
}
