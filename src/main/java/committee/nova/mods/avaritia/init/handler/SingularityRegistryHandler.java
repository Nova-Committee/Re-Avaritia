package committee.nova.mods.avaritia.init.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.common.net.SyncSingularitiesPacket;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.util.SingularityUtil;
import io.github.fabricators_of_create.porting_lib.entity.events.OnDatapackSyncCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:35
 * Version: 1.0
 */
public class SingularityRegistryHandler {
    private static final SingularityRegistryHandler INSTANCE = new SingularityRegistryHandler();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    private final Map<ResourceLocation, Singularity> singularities = new LinkedHashMap<>();

    public static SingularityRegistryHandler getInstance() {
        return INSTANCE;
    }

    public static void init(){
        onResourceManagerReload();
        onDataPackSync();
    }

    private static void onDataPackSync() {
        OnDatapackSyncCallback.EVENT.register((list, player) -> {
            var message = new SyncSingularitiesPacket(SingularityRegistryHandler.getInstance().getSingularities());

            if (player != null) {
                NetworkHandler.getChannel().sendToClient(message, player);
            } else {
                NetworkHandler.getChannel().sendToClients(message, list.getPlayers());
            }
        });

    }

    public static void onResourceManagerReload() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            (new SingularityRegistryHandler()).loadSingularities();
        });
    }

    private void loadSingularities() {
        var stopwatch = Stopwatch.createStarted();
        var dir = FabricLoader.getInstance().getConfigDir().resolve("avaritia/singularities/").toFile();

        this.writeDefaultSingularityFiles();

        this.singularities.clear();

        if (!dir.mkdirs() && dir.isDirectory()) {
            this.loadFiles(dir);
        }

        stopwatch.stop();

        Static.LOGGER.info("Loaded {} singularity type(s) in {} ms", this.singularities.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void writeDefaultSingularityFiles() {
        var dir = FabricLoader.getInstance().getConfigDir().resolve("avaritia/singularities/").toFile();

        if (!dir.exists() && dir.mkdirs()) {
            Static.LOGGER.warn("Could not find default singularities,try to generate!");
            for (var singularity : ModSingularities.getDefaults()) {
                var json = SingularityUtil.writeToJson(singularity);
                FileWriter writer = null;

                try {
                    var file = new File(dir, singularity.getId().getPath() + ".json");
                    writer = new FileWriter(file, StandardCharsets.UTF_8);

                    GSON.toJson(json, writer);
                    writer.close();
                } catch (Exception e) {
                    Static.LOGGER.error("An error occurred while generating default singularities", e);
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

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.singularities.size());

        this.singularities.forEach((id, singularity) -> {
            singularity.write(buffer);
        });
    }

    public List<Singularity> readFromBuffer(FriendlyByteBuf buffer) {
        List<Singularity> singularities = new ArrayList<>();

        int size = buffer.readVarInt();

        for (int i = 0; i < size; i++) {
            var singularity = Singularity.read(buffer);

            singularities.add(singularity);
        }

        return singularities;
    }

    public void loadSingularities(SyncSingularitiesPacket message) {
        var singularities = message.getSingularities()
                .stream()
                .collect(Collectors.toMap(Singularity::getId, s -> s));

        this.singularities.clear();
        this.singularities.putAll(singularities);

        Static.LOGGER.info("Loaded {} singularities from the server", singularities.size());
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

                singularity = SingularityUtil.loadFromJson(new ResourceLocation(Static.MOD_ID, name), json);

                reader.close();
            } catch (Exception e) {
                Static.LOGGER.error("An error occurred while loading singularities", e);
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
