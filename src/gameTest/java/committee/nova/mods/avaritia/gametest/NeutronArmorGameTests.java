package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.input.ExtremeSmithingRecipeInput;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.network.PacketSendListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

/** Loaded recipe, real owner interactions, damage and tick paths for 1.21.1 wolf armor. */
@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeutronArmorGameTests {
    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void recipes_and_material(GameTestHelper h) { recipe(h); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_owner_equip(GameTestHelper h) { equipWolf(h); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_guard(GameTestHelper h) { guard(h); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_hax(GameTestHelper h) { hax(h); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_totem(GameTestHelper h) { totem(h, true); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_no_durability(GameTestHelper h) { totem(h, false); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_bond(GameTestHelper h) { bond(h, false); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_cooldown_reequip(GameTestHelper h) { bond(h, true); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void wolf_health_remove(GameTestHelper h) { health(h); }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void infinity_and_kill_bypass(GameTestHelper h) {
        Wolf infinity = wolf(h);
        Wolf kill = wolf(h);
        infinity.setHealth(1);
        kill.setHealth(1);
        infinity.hurt(h.getLevel().damageSources().source(
                committee.nova.mods.avaritia.init.registry.ModDamageTypes.INFINITY), 1000);
        kill.hurt(h.getLevel().damageSources().genericKill(), 1000);
        h.assertTrue(infinity.isDeadOrDying() && kill.isDeadOrDying(), "infinity and vanilla kill must bypass wolf guard/totem");
        h.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void ordinary_damage_keeps_armor_durability(GameTestHelper h) {
        Wolf wolf = wolf(h);
        int before = wolf.getBodyArmorItem().getDamageValue();
        wolf.hurt(h.getLevel().damageSources().generic(), 2);
        h.assertTrue(wolf.getBodyArmorItem().getDamageValue() == before,
                "existing WolfMixin must retain the 1.21.1 no-ordinary-wear behavior");
        h.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void nonlethal_armor_hit_preserves_bond(GameTestHelper h) {
        Wolf wolf = wolf(h);
        ServerPlayer owner = player(h, "ArmoredOwner");
        wolf.tame(owner);
        owner.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        owner.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        owner.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        owner.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        // Vanilla installs equipment attribute modifiers during entity tick.
        h.runAfterDelay(2, () -> {
            try {
                // In production the real packet listener invokes doTick; this transport-only listener is not network-ticked.
                owner.doTick();
                h.assertTrue(owner.getArmorValue() == 20, "diamond armor attributes must be active before damage");
                owner.setHealth(10);
                boolean hit = owner.hurt(h.getLevel().damageSources().mobAttack(wolf), 15);
                h.assertTrue(hit && owner.getHealth() > 0 && owner.getHealth() < 10,
                        "armor must reduce raw lethal damage to a real nonlethal hit");
                h.assertTrue(wolf.getData(ModDataAttachments.WOLF_ARMOR_STATE).bondReadyAt() == 0,
                        "post-armor nonlethal damage must not consume bond cooldown");
                h.succeed();
            } finally {
                remove(owner);
            }
        });
    }

    private static void recipe(GameTestHelper h) {
        var holder = h.getLevel().getRecipeManager().byKey(Const.rl("neutron_wolf_armor")).orElseThrow();
        h.assertTrue(holder.value() instanceof ExtremeSmithingRecipe, "wolf smithing recipe must decode");
        var recipe = (ExtremeSmithingRecipe) holder.value();
        ItemStack base = new ItemStack(Items.WOLF_ARMOR);
        h.assertTrue(recipe.isBaseIngredient(base), "recipe must consume vanilla wolf armor");
        base.set(DataComponents.CUSTOM_NAME, Component.literal("Armor port"));
        ItemStack result = recipe.assemble(new ExtremeSmithingRecipeInput(
                new ItemStack(ModItems.upgrade_smithing_template.get()), base, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY), h.getLevel().registryAccess());
        h.assertTrue(result.is(ModItems.neutron_wolf_armor.get()) && result.getMaxDamage() == 8888, "loaded recipe must produce registered 8888-durability armor");
        h.assertTrue("Armor port".equals(result.getHoverName().getString()), "smithing retains name");
        var item = (net.minecraft.world.item.AnimalArmorItem) result.getItem();
        h.assertTrue(item.getTexture().equals(Const.rl("textures/entity/wolf/wolf_armor.png")), "animal armor must resolve existing wolf texture");
        var path = net.neoforged.fml.ModList.get().getModFileById("avaritia").getFile()
                .findResource("assets", "avaritia", item.getTexture().getPath());
        try (var texture = java.nio.file.Files.newInputStream(path)) {
            h.assertTrue(texture != null && texture.read() == 137 && texture.read() == 80, "resolved entity texture must open as PNG");
        } catch (java.io.IOException failure) {
            throw new IllegalStateException(failure);
        }
        h.succeed();
    }

    private static Wolf wolf(GameTestHelper h) {
        Wolf wolf = h.spawn(EntityType.WOLF, new BlockPos(2, 2, 2));
        wolf.setNoAi(true);
        wolf.setNoGravity(true);
        wolf.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_wolf_armor.get()));
        return wolf;
    }

    private static ServerPlayer player(GameTestHelper h, String name) {
        ServerLevel level = h.getLevel();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        new NoLoginListener(level, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = false;
        player.invulnerableTime = 0;
        // Skip only the login grace period; keep the concrete ServerPlayer damage implementation.
        try {
            var grace = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            grace.setAccessible(true);
            grace.setInt(player, 0);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
        BlockPos pos = h.absolutePos(new BlockPos(3, 2, 2));
        player.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        level.addNewPlayer(player);
        return player;
    }

    private static void remove(ServerPlayer player) {
        ((ServerLevel) player.level()).removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }

    private static void equipWolf(GameTestHelper h) {
        Wolf wolf = wolf(h);
        wolf.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
        ServerPlayer owner = player(h, "ArmorOwner");
        ServerPlayer stranger = player(h, "ArmorOther");
        try {
            wolf.tame(owner);
            wolf.setTame(true, true);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.neutron_wolf_armor.get()));
            stranger.interactOn(wolf, InteractionHand.MAIN_HAND);
            h.assertTrue(wolf.getBodyArmorItem().isEmpty(), "a stranger must not equip another player's wolf");
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.neutron_wolf_armor.get()));
            h.assertTrue(owner.interactOn(wolf, InteractionHand.MAIN_HAND).consumesAction(), "owner interaction must equip");
            h.assertTrue(wolf.getBodyArmorItem().is(ModItems.neutron_wolf_armor.get())
                    && owner.getMainHandItem().isEmpty(), "owner equips one armor and consumes one item");
        } finally {
            remove(owner);
            remove(stranger);
        }
        h.succeed();
    }

    private static void guard(GameTestHelper h) {
        Wolf wolf = wolf(h);
        ServerPlayer owner = player(h, "GuardOwner");
        try {
            wolf.tame(owner);
            float before = wolf.getHealth();
            boolean hurt = wolf.hurt(h.getLevel().damageSources().playerAttack(owner), 100);
            h.assertTrue(!hurt && wolf.getHealth() == before && wolf.hurtTime == 0,
                    "nearby ordinary damage must cancel before health and hurt animation");
            h.assertTrue(wolf.getBodyArmorItem().getDamageValue() == 0, "guard must also avoid ordinary armor wear");
        } finally {
            remove(owner);
        }
        h.succeed();
    }

    private static void hax(GameTestHelper h) {
        Wolf wolf = wolf(h);
        ServerPlayer owner = player(h, "HaxOwner");
        try {
            wolf.tame(owner);
            wolf.setHealth(wolf.getMaxHealth());
            float before = wolf.getHealth();
            wolf.hurt(h.getLevel().damageSources().sonicBoom(owner), 2);
            float expected = 2 * (1 - ModConfig.neutronWolfHaxReduction.get().floatValue());
            h.assertTrue(Math.abs((before - wolf.getHealth()) - expected) < 0.001,
                    "bypassing damage must retain the configured cap, not full immunity");
        } finally {
            remove(owner);
        }
        h.succeed();
    }

    private static void totem(GameTestHelper h, boolean payable) {
        Wolf wolf = wolf(h);
        ItemStack armor = wolf.getBodyArmorItem();
        if (!payable) armor.setDamageValue(armor.getMaxDamage() - 100);
        int before = armor.getDamageValue();
        wolf.setHealth(1);
        wolf.hurt(h.getLevel().damageSources().magic(), 100);
        if (payable) {
            h.assertTrue(wolf.isAlive() && wolf.getHealth() == wolf.getMaxHealth(), "totem must save and fully heal wolf");
            h.assertTrue(armor.getDamageValue() - before == 500, "magic lethal hit must cost exactly 500 durability");
        } else {
            h.assertTrue(wolf.isDeadOrDying(), "insufficient durability must not save wolf");
        }
        h.succeed();
    }

    private static void bond(GameTestHelper h, boolean repeat) {
        Wolf wolf = wolf(h);
        ServerPlayer owner = player(h, repeat ? "CooldownOwner" : "BondOwner");
        try {
            wolf.tame(owner);
            owner.setHealth(1);
            owner.hurt(h.getLevel().damageSources().magic(), 100);
            h.assertTrue(!owner.isDeadOrDying() && owner.getHealth() == 1,
                    "real ServerPlayer hurt path must be rescued by owned nearby wolf");
            long ready = wolf.getData(ModDataAttachments.WOLF_ARMOR_STATE).bondReadyAt();
            h.assertTrue(ready == h.getLevel().getGameTime() + 60, "bond must save its cooldown on wolf");
            if (repeat) {
                ItemStack armor = wolf.getBodyArmorItem();
                wolf.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
                wolf.setItemSlot(EquipmentSlot.BODY, armor);
                h.assertTrue(wolf.getData(ModDataAttachments.WOLF_ARMOR_STATE).bondReadyAt() == ready,
                        "reequip must retain entity cooldown");
                owner.invulnerableTime = 0;
                owner.hurt(h.getLevel().damageSources().magic(), 100);
                h.assertTrue(owner.isDeadOrDying(), "second lethal hit during cooldown must not be rescued");
            }
        } finally {
            remove(owner);
        }
        h.succeed();
    }

    private static void health(GameTestHelper h) {
        Wolf wolf = wolf(h);
        double base = wolf.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
        h.runAfterDelay(25, () -> {
            h.assertTrue(Math.abs(wolf.getMaxHealth() - base - 100) < 0.001, "wearing armor must add 100 max health on tick");
            wolf.setHealth(wolf.getMaxHealth());
            wolf.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
            h.runAfterDelay(25, () -> {
                h.assertTrue(wolf.getMaxHealth() == base && wolf.getHealth() <= base,
                        "removing armor must remove only its bonus and clamp health");
                h.succeed();
            });
        });
    }

    /** Transport stub only; ServerPlayer damage, health, death and event code stay real. */
    private static final class NoLoginListener extends ServerGamePacketListenerImpl {
        private NoLoginListener(ServerLevel level, ServerPlayer player, CommonListenerCookie cookie) {
            super(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie);
        }

        @Override
        public void send(Packet<?> packet) { }

        @Override
        public void send(Packet<?> packet, PacketSendListener listener) { }
    }
}
