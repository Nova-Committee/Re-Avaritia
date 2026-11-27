package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.input.ExtremeSmithingRecipeInput;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import java.util.UUID;

/** Equipment, recipes and real server damage/tick paths for the two neutron armors. */
public final class NeutronArmorGameTests {
    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void recipes_and_equipment(GameTestHelper h) { recipes(h); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_owner_equip(GameTestHelper h) { equipWolf(h); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_guard(GameTestHelper h) { guard(h); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_hax(GameTestHelper h) { hax(h); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_totem(GameTestHelper h) { totem(h, true); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_no_durability(GameTestHelper h) { totem(h, false); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_bond(GameTestHelper h) { bond(h, false); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_cooldown_reequip(GameTestHelper h) { bond(h, true); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void wolf_health_remove(GameTestHelper h) { health(h); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void nautilus_air_speed_remove(GameTestHelper h) { airAndSpeed(h); }

    @GameTest(structure = "avaritia_armor_test:armor_empty", maxTicks = 100)
    public void nautilus_water_damage(GameTestHelper h) { waterDamage(h); }

    private static void recipes(GameTestHelper h) {
        String[] names = {"neutron_wolf_armor", "neutron_nautilus_armor"};
        ItemStack[] bases = {new ItemStack(Items.WOLF_ARMOR), new ItemStack(Items.NETHERITE_NAUTILUS_ARMOR)};
        for (int i = 0; i < names.length; i++) {
            var holder = h.getLevel().getServer().getRecipeManager()
                    .byKey(ResourceKey.create(Registries.RECIPE, Const.rl(names[i]))).orElseThrow();
            h.assertTrue(holder.value() instanceof ExtremeSmithingRecipe, "armor recipe must decode in RecipeManager");
            var recipe = (ExtremeSmithingRecipe) holder.value();
            h.assertTrue(recipe.isBaseIngredient(bases[i]), "recipe must consume the original vanilla armor");
            bases[i].set(DataComponents.CUSTOM_NAME, Component.literal("Armor port"));
            var input = new ExtremeSmithingRecipeInput(new ItemStack(ModItems.upgrade_smithing_template.get()),
                    bases[i], ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
            ItemStack result = recipe.assemble(input);
            h.assertTrue(result.is(i == 0 ? ModItems.neutron_wolf_armor.get() : ModItems.neutron_nautilus_armor.get()),
                    "loaded recipe must assemble the registered armor");
            h.assertTrue("Armor port".equals(result.getHoverName().getString()), "smithing must retain the input name");
            var equipment = result.get(DataComponents.EQUIPPABLE);
            h.assertTrue(equipment != null && equipment.slot() == EquipmentSlot.BODY,
                    "result must expose the vanilla body equipment component");
            h.assertTrue(equipment.assetId().orElseThrow().identifier().equals(Const.rl(names[i])),
                    "each armor must resolve its own equipment asset");
            h.assertTrue(equipment.canBeEquippedBy((i == 0 ? EntityTypes.WOLF : EntityTypes.NAUTILUS).builtInRegistryHolder()),
                    "equipment restrictions must allow the intended animal");
            if (i == 0) h.assertTrue(result.getMaxDamage() == 8888, "wolf armor must have 8888 durability");
        }
        h.succeed();
    }

    private static Wolf wolf(GameTestHelper h) {
        Wolf wolf = h.spawn(EntityTypes.WOLF, new BlockPos(2, 2, 2));
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
        player.setInvulnerableTime(0);
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
            wolf.setOwner(owner);
            wolf.setTame(true, true);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.neutron_wolf_armor.get()));
            stranger.interactOn(wolf, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
            h.assertTrue(wolf.getBodyArmorItem().isEmpty(), "a stranger must not equip another player's wolf");
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.neutron_wolf_armor.get()));
            h.assertTrue(owner.interactOn(wolf, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO).consumesAction(), "owner interaction must equip");
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
            wolf.setOwner(owner);
            float before = wolf.getHealth();
            boolean hurt = wolf.hurtServer(h.getLevel(), h.getLevel().damageSources().playerAttack(owner), 100);
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
            wolf.setOwner(owner);
            wolf.setHealth(wolf.getMaxHealth());
            float before = wolf.getHealth();
            wolf.hurtServer(h.getLevel(), h.getLevel().damageSources().sonicBoom(owner), 2);
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
        wolf.hurtServer(h.getLevel(), h.getLevel().damageSources().magic(), 100);
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
            wolf.setOwner(owner);
            owner.setHealth(1);
            owner.hurtServer(h.getLevel(), h.getLevel().damageSources().magic(), 100);
            h.assertTrue(!owner.isDeadOrDying() && owner.getHealth() == 1,
                    "real ServerPlayer hurt path must be rescued by owned nearby wolf");
            long ready = ((AttachmentTarget) wolf).getAttached(ModDataAttachments.WOLF_ARMOR_STATE).bondReadyAt();
            h.assertTrue(ready == h.getLevel().getGameTime() + 60, "bond must save its cooldown on wolf");
            if (repeat) {
                ItemStack armor = wolf.getBodyArmorItem();
                wolf.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
                wolf.setItemSlot(EquipmentSlot.BODY, armor);
                h.assertTrue(((AttachmentTarget) wolf).getAttached(ModDataAttachments.WOLF_ARMOR_STATE).bondReadyAt() == ready,
                        "reequip must retain entity cooldown");
                owner.setInvulnerableTime(0);
                owner.damageCooldownTime = 0;
                owner.hurtServer(h.getLevel(), h.getLevel().damageSources().magic(), 100);
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

    private static void airAndSpeed(GameTestHelper h) {
        var armored = h.spawn(EntityTypes.NAUTILUS, new BlockPos(2, 2, 2));
        var control = h.spawn(EntityTypes.NAUTILUS, new BlockPos(5, 2, 5));
        armored.setNoGravity(true);
        control.setNoGravity(true);
        // Leave AI enabled: Nautilus.baseTick skips real air consumption when NoAI is set.
        armored.setAirSupply(100);
        control.setAirSupply(100);
        armored.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_nautilus_armor.get()));
        double base = armored.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        h.runAfterDelay(25, () -> {
            h.assertTrue(!armored.isInWater() && armored.getAirSupply() == armored.getMaxAirSupply(),
                    "actual land ticks must keep armored nautilus air full");
            h.assertTrue(control.getAirSupply() < 100, "unarmored control must consume air on the same land ticks");
            h.assertTrue(Math.abs(armored.getAttributeValue(Attributes.MOVEMENT_SPEED) - base * 1.5) < 0.0001,
                    "actual entity ticks must grant 50 percent speed");
            armored.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
            armored.setAirSupply(100);
            h.runAfterDelay(25, () -> {
                h.assertTrue(Math.abs(armored.getAttributeValue(Attributes.MOVEMENT_SPEED) - base) < 0.0001,
                        "removing armor must remove its speed bonus");
                h.assertTrue(armored.getAirSupply() < 100, "air must resume consumption after armor removal");
                h.succeed();
            });
        });
    }

    private static void waterDamage(GameTestHelper h) {
        for (int x = 0; x <= 4; x++) for (int y = 0; y <= 4; y++) for (int z = 0; z <= 4; z++)
            h.setBlock(new BlockPos(x, y, z), x == 0 || x == 4 || y == 0 || y == 4 || z == 0 || z == 4
                    ? Blocks.GLASS : Blocks.WATER);
        var wet = h.spawn(EntityTypes.NAUTILUS, new BlockPos(2, 2, 2));
        var dry = h.spawn(EntityTypes.NAUTILUS, new BlockPos(6, 2, 2));
        wet.setNoAi(true);
        dry.setNoAi(true);
        wet.setNoGravity(true);
        dry.setNoGravity(true);
        wet.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_nautilus_armor.get()));
        dry.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_nautilus_armor.get()));
        h.runAfterDelay(3, () -> {
            h.assertTrue(wet.isInWater() && !dry.isInWater(), "fixture must exercise real wet and dry states: wet=" + wet.isInWater() + ", dry=" + dry.isInWater());
            float wetBefore = wet.getHealth();
            float dryBefore = dry.getHealth();
            // Bypasses vanilla armor and enchantments, so this isolates incoming reduction.
            wet.hurtServer(h.getLevel(), h.getLevel().damageSources().genericKill(), 10);
            dry.hurtServer(h.getLevel(), h.getLevel().damageSources().genericKill(), 10);
            h.assertTrue(Math.abs(wetBefore - wet.getHealth() - 0.1) < 0.001,
                    "underwater incoming damage must be reduced by 99 percent");
            h.assertTrue(Math.abs(dryBefore - dry.getHealth() - 10) < 0.001,
                    "dry nautilus must not receive water damage reduction");
            h.succeed();
        });
    }

    /** Transport stub only; ServerPlayer damage, health, death and event code stay real. */
    private static final class NoLoginListener extends ServerGamePacketListenerImpl {
        private NoLoginListener(ServerLevel level, ServerPlayer player, CommonListenerCookie cookie) {
            super(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie);
        }

        @Override
        public boolean hasClientLoaded() { return true; }

        @Override
        public void send(Packet<?> packet) { }

        @Override
        public void send(Packet<?> packet, ChannelFutureListener listener) { }
    }
}
