package committee.nova.mods.avaritia.init.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.common.net.S2CSingularitiesPack;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:35
 * Version: 1.0
 */
@EventBusSubscriber
public class SingularityRegistryHandler {
    private static final SingularityRegistryHandler INSTANCE = new SingularityRegistryHandler();

    private final Map<ResourceLocation, Singularity> singularities = new LinkedHashMap<>();

    public static SingularityRegistryHandler getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        var message = new S2CSingularitiesPack(SingularityRegistryHandler.getInstance().getSingularities());
        var player = event.getPlayer();

        if (player != null) {
            PacketDistributor.sendToPlayer(player, message);
        } else {
            PacketDistributor.sendToAllPlayers(message);
        }
    }

    public void loadSingularities() {
        var stopwatch = Stopwatch.createStarted();
        var dir = FMLPaths.CONFIGDIR.get().resolve("avaritia/singularities/").toFile();

        this.writeDefaultSingularityFiles();

        this.singularities.clear();

        if (!dir.mkdirs() && dir.isDirectory()) {
            this.loadFiles(dir);
        }

        EternalSingularityCraftRecipe.invalidate();

        stopwatch.stop();

        Const.LOGGER.info("Loaded {} singularity type(s) in {} ms", this.singularities.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void writeDefaultSingularityFiles() {
        var dir = FMLPaths.CONFIGDIR.get().resolve("avaritia" + File.separator + "singularities").toFile();

        if (!dir.exists() && dir.mkdirs()) {
            Const.LOGGER.warn("Could not find default singularities,try to generate!");
            for (var singularity : ModSingularities.getDefaults()) {
                var json = SingularityUtils.writeToJson(singularity);
                FileWriter writer = null;

                try {
                    var file = new File(dir, singularity.getId().getPath() + ".json");
                    writer = new FileWriter(file);

                    GSON.toJson(json, writer);
                    writer.close();
                } catch (Exception e) {
                    Const.LOGGER.error("An error occurred while generating default singularities", e);
                } finally {
                    IOUtils.closeQuietly(writer);
                }
            }
        }
    }

    public List<Singularity> getSingularities() {
        return Lists.newArrayList(this.singularities.values());
    }

    public Singularity getSingularityById(ResourceLocation id) {
        return this.singularities.get(id);
    }

    public void loadSingularities(S2CSingularitiesPack message) {
        var singularities = message.singularities()
                .stream()
                .collect(Collectors.toMap(Singularity::getId, s -> s));

        this.singularities.clear();
        this.singularities.putAll(singularities);
        EternalSingularityCraftRecipe.invalidate();

        Const.LOGGER.info("Loaded {} singularities from the server", singularities.size());
    }

    private void loadFiles(File dir) {
        var files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            Singularity singularity = null;

            try {
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                var name = file.getName().replace(".json", "");
                json = JsonParser.parseReader(reader).getAsJsonObject();

                singularity = SingularityUtils.loadFromJson(Const.rl(name), json);

                reader.close();
            } catch (Exception e) {
                Const.LOGGER.error("An error occurred while loading singularities", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (singularity != null && singularity.isEnabled()) {
                var id = singularity.getId();

                this.singularities.put(id, singularity);
            }
        }
    }
}
