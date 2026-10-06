package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.init.data.provider.RetainedResourcesProvider;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import java.io.IOException;
import java.nio.file.Path;

/** Retained, checked-in resources are the authoritative data/art source for this port. */
public final class ModDataGen {
    public static void register(DataGenerator generator, Path projectRoot) {
        generator.getVanillaPack(true).addProvider(output -> new RetainedResourcesProvider(output, projectRoot));
    }

    /** Usage: ModDataGen <project root> <separate output directory>. */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) throw new IllegalArgumentException("Expected project root and output directory");
        SharedConstants.tryDetectVersion();
        DataGenerator generator = new DataGenerator(Path.of(args[1]), SharedConstants.getCurrentVersion(), false);
        register(generator, Path.of(args[0]));
        generator.run();
    }

    private ModDataGen() {}
}
