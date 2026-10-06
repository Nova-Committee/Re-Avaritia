package committee.nova.mods.avaritia.init.data.provider;

import com.google.common.hash.Hashing;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Exports all retained datapack and artwork files using vanilla's cached-output protocol. */
public final class RetainedResourcesProvider implements DataProvider {
    private final Path output;
    private final Path projectRoot;

    public RetainedResourcesProvider(PackOutput output, Path projectRoot) {
        this.output = output.getOutputFolder().toAbsolutePath().normalize();
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            Map<Path, Path> files = new LinkedHashMap<>();
            try {
                collect(projectRoot.resolve("src/generated/resources"), files);
                collect(projectRoot.resolve("src/main/resources"), files);
                for (Map.Entry<Path, Path> file : files.entrySet()) {
                    byte[] bytes = Files.readAllBytes(file.getValue());
                    cache.writeIfNeeded(output.resolve(file.getKey()), bytes, Hashing.sha1().hashBytes(bytes));
                }
            } catch (IOException exception) {
                throw new CompletionException(exception);
            }
        }, Util.backgroundExecutor());
    }

    private void collect(Path root, Map<Path, Path> files) throws IOException {
        if (output.startsWith(root)) throw new IllegalArgumentException("Datagen output must not overwrite retained resources: " + root);
        Path metadata = root.resolve("pack.mcmeta");
        if (Files.isRegularFile(metadata)) files.put(Path.of("pack.mcmeta"), metadata);
        for (String folder : new String[]{"assets", "data"}) {
            Path source = root.resolve(folder);
            if (!Files.isDirectory(source)) continue;
            try (var paths = Files.walk(source)) {
                paths.filter(Files::isRegularFile).sorted().forEach(path -> files.put(root.relativize(path), path));
            }
        }
    }

    @Override
    public String getName() { return "Avaritia retained resources"; }
}
