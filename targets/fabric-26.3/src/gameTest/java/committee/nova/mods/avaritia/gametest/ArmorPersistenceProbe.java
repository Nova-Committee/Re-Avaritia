package committee.nova.mods.avaritia.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import committee.nova.mods.avaritia.common.component.WolfArmorState;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/** Two-process probe, enabled only by the isolated persistence launch configuration. */
public final class ArmorPersistenceProbe implements ModInitializer {
    @Override
    public void onInitialize() {
        if (PHASE.isEmpty()) return;
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try { started(server); } catch (IOException ex) { throw new IllegalStateException(ex); }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try { tick(server); } catch (IOException ex) { throw new IllegalStateException(ex); }
        });
    }
    private static final String PHASE = System.getProperty("avaritia.armor.persistence", "");
    private static int ticks;
    private static UUID expectedWolf;
    private static long expectedReady;
    private static boolean complete;

    public static void started(MinecraftServer server) throws IOException {
        if (PHASE.isEmpty()) return;
        ServerLevel level = server.overworld();
        level.getChunk(0, 0);
        level.setChunkForced(0, 0, true);
        if (PHASE.equals("write")) {
            Wolf wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.COMMAND);
            if (wolf == null) throw new IllegalStateException("wolf creation failed");
            wolf.setPos(8, 80, 8);
            wolf.setNoAi(true);
            wolf.setNoGravity(true);
            wolf.setPersistenceRequired();
            wolf.setCustomName(Component.literal("ArmorPersistenceProbe"));
            ItemStack armor = new ItemStack(ModItems.neutron_wolf_armor.get());
            armor.setDamageValue(500);
            wolf.setItemSlot(EquipmentSlot.BODY, armor);
            expectedWolf = wolf.getUUID();
            expectedReady = level.getGameTime() + 72000;
            ((AttachmentTarget) wolf).setAttached(ModDataAttachments.WOLF_ARMOR_STATE, new WolfArmorState(expectedReady));
            level.addFreshEntity(wolf);
        } else if (PHASE.equals("read")) {
            JsonObject previous = JsonParser.parseString(Files.readString(output().resolve("write.json"))).getAsJsonObject();
            expectedWolf = UUID.fromString(previous.get("wolf").getAsString());
            expectedReady = previous.get("bondReadyAt").getAsLong();
        } else {
            throw new IllegalArgumentException("unknown persistence probe phase: " + PHASE);
        }
    }

    public static void tick(MinecraftServer server) throws IOException {
        if (PHASE.isEmpty() || complete) return;
        ticks++;
        ServerLevel level = server.overworld();
        Wolf wolf = null;
        for (Entity entity : level.getAllEntities()) {
            if (entity.getUUID().equals(expectedWolf) && entity instanceof Wolf candidate) wolf = candidate;
        }
        if (wolf == null) {
            if (ticks > 400) throw new IllegalStateException("persisted wolf failed to load: " + expectedWolf);
            return;
        }
        if (ticks < 40) return;
        WolfArmorState state = ((AttachmentTarget) wolf).getAttached(ModDataAttachments.WOLF_ARMOR_STATE);
        if (state == null || state.bondReadyAt() != expectedReady || state.isBondReady(level.getGameTime()))
            throw new IllegalStateException("bond cooldown failed persistence: " + state);
        ItemStack armor = wolf.getBodyArmorItem();
        if (!armor.is(ModItems.neutron_wolf_armor.get()) || armor.getDamageValue() != 500
                || !"ArmorPersistenceProbe".equals(wolf.getName().getString()))
            throw new IllegalStateException("equipment or unrelated name failed persistence");
        // Detaching and reattaching the saved item must not recreate the entity attachment.
        wolf.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
        wolf.setItemSlot(EquipmentSlot.BODY, armor);
        if (((AttachmentTarget) wolf).getAttached(ModDataAttachments.WOLF_ARMOR_STATE).bondReadyAt() != expectedReady)
            throw new IllegalStateException("reequip reset saved cooldown");
        JsonObject result = new JsonObject();
        result.addProperty("phase", PHASE);
        result.addProperty("wolf", wolf.getUUID().toString());
        result.addProperty("bondReadyAt", state.bondReadyAt());
        result.addProperty("gameTime", level.getGameTime());
        result.addProperty("armorDamage", armor.getDamageValue());
        result.addProperty("cooldownStillActive", true);
        result.addProperty("reequipPreserved", true);
        Files.createDirectories(output());
        Files.writeString(output().resolve(PHASE + ".json"), result.toString() + "\n");
        complete = true;
        server.saveEverything(false, true, true);
        System.out.println("ARMOR_PERSISTENCE_PASS " + result);
        server.halt(false);
    }

    private static Path output() {
        return Path.of(System.getProperty("avaritia.armor.evidence"));
    }
}
