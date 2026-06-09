package committee.nova.mods.avaritia.client.render.mesh;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Simple OBJ mesh loader")
class SimpleObjMeshLoaderTest {
    private static final float EPSILON = 0.0001F;

    @Test
    @DisplayName("parses quad faces with legacy vertex order and flipped V coordinates")
    void parseLinesKeepsLegacyQuadOrderAndFlipsUvV() {
        Map<String, SimpleMesh> meshes = SimpleObjMeshLoader.parseLines(List.of(
                "o model",
                "v 1 2 3",
                "v 4 5 6",
                "v 7 8 9",
                "v 10 11 12",
                "vt 0.1 0.2",
                "vt 0.3 0.4",
                "vt 0.5 0.6",
                "vt 0.7 0.8",
                "vn 0 1 0",
                "f 1/1/1 2/2/1 3/3/1 4/4/1"
        ), false);

        SimpleMesh mesh = meshes.get("model");
        assertEquals(4, mesh.vertexCount());
        SimpleMesh.Vertex first = mesh.vertices().get(0);
        SimpleMesh.Vertex second = mesh.vertices().get(1);

        assertAll(
                () -> assertEquals(1.0F, first.x(), EPSILON),
                () -> assertEquals(2.0F, first.y(), EPSILON),
                () -> assertEquals(3.0F, first.z(), EPSILON),
                () -> assertEquals(0.1F, first.u(), EPSILON),
                () -> assertEquals(0.8F, first.v(), EPSILON),
                () -> assertEquals(10.0F, second.x(), EPSILON),
                () -> assertEquals(11.0F, second.y(), EPSILON),
                () -> assertEquals(12.0F, second.z(), EPSILON),
                () -> assertEquals(0.7F, second.u(), EPSILON),
                () -> assertEquals(0.2F, second.v(), EPSILON)
        );
    }

    @Test
    @DisplayName("applies swapYZ to positions and normals")
    void parseLinesAppliesSwapYzToPositionsAndNormals() {
        Map<String, SimpleMesh> meshes = SimpleObjMeshLoader.parseLines(List.of(
                "o model",
                "v 1 2 3",
                "v 4 5 6",
                "v 7 8 9",
                "v 10 11 12",
                "vt 0 0",
                "vn 0 1 0",
                "f 1/1/1 2/1/1 3/1/1 4/1/1"
        ), true);

        SimpleMesh.Vertex first = meshes.get("model").vertices().get(0);
        assertAll(
                () -> assertEquals(1.0F, first.x(), EPSILON),
                () -> assertEquals(3.0F, first.y(), EPSILON),
                () -> assertEquals(2.0F, first.z(), EPSILON),
                () -> assertEquals(0.0F, first.normalX(), EPSILON),
                () -> assertEquals(0.0F, first.normalY(), EPSILON),
                () -> assertEquals(1.0F, first.normalZ(), EPSILON)
        );
    }

    @Test
    @DisplayName("keeps duplicate OBJ object names addressable")
    void parseLinesKeepsDuplicateObjectNames() {
        Map<String, SimpleMesh> meshes = SimpleObjMeshLoader.parseLines(List.of(
                "o part",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "v 0 1 0",
                "f 1 2 3 4",
                "o part",
                "f 1 2 3 4"
        ), false);

        assertAll(
                () -> assertTrue(meshes.containsKey("part")),
                () -> assertTrue(meshes.containsKey("part0"))
        );
    }
}
