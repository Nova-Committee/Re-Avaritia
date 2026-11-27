package committee.nova.mods.avaritia.client.shader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Black hole shader compatibility")
class BlackHoleShaderCompatibilityTest {
    private static final String SHADER_PATH = "assets/avaritia/shaders/core/black_hole.fsh";
    private static final Pattern BUILT_IN_NOISE_DECLARATION = Pattern.compile(
            "\\b(?:float|vec[234])\\s+noise[1-4]\\s*\\("
    );

    @Test
    @DisplayName("does not overload GLSL built-in noise functions")
    void avoidsBuiltInNoiseFunctionNames() throws IOException {
        String source = loadShader();

        assertAll(
                () -> assertFalse(BUILT_IN_NOISE_DECLARATION.matcher(source).find(),
                        "GLSL noise1-noise4 are built-ins and cannot be overloaded with a different return type"),
                () -> assertEquals(3, countOccurrences(source, "avaritiaValueNoise("),
                        "the project-specific noise function must keep one declaration and two call sites")
        );
    }

    private static String loadShader() throws IOException {
        try (InputStream stream = BlackHoleShaderCompatibilityTest.class.getClassLoader().getResourceAsStream(SHADER_PATH)) {
            assertNotNull(stream, "missing shader resource: " + SHADER_PATH);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int countOccurrences(String source, String needle) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
