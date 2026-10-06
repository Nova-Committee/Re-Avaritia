package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingKeys;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.init.registry.ModItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.UUID;

/** Registered only in the optional Trinkets runtime profile. */
public final class NativeTrinketsGameTests {
    private static final String TEMPLATE = "avaritia:portable_ui_empty";

    private NativeTrinketsGameTests() {}

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void wornRingCreatesAndDeletesPersonalDimension(GameTestHelper helper) {
        ServerPlayer player = player(helper, "TrinketsRing");
        equip(helper, player, "hand", "ring", new ItemStack(ModItems.infinity_ring.get()));
        helper.assertTrue(player.getInventory().isEmpty(), "ring must be worn, not carried in the vanilla inventory");
        helper.assertTrue(InfinityRingDimensions.create(player, InfinityRingSettings.Terrain.VOID,
                        InfinityRingSettings.TimeMode.DAY, InfinityRingSettings.WeatherMode.CLEAR,
                        InfinityRingSettings.Access.PRIVATE),
                "a worn ring must authorize actual personal-dimension creation");
        helper.assertTrue(InfinityRingKeys.ownerOf(player.level().dimension()).filter(player.getUUID()::equals).isPresent(),
                "worn-ring creation must transport the player to their own dimension");
        InfinityRingDimensions.deleteOwn(player).whenComplete((deleted, failure) ->
                helper.runAfterDelay(1, () -> {
                    remove(player);
                    if (failure != null) helper.fail(failure.toString());
                    else {
                        helper.assertTrue(Boolean.TRUE.equals(deleted), "a worn ring must authorize deletion and return travel");
                        helper.assertTrue(player.server.getLevel(InfinityRingKeys.levelKey(player.getUUID())) == null,
                                "deleted personal dimension must no longer be live");
                        helper.succeed();
                    }
                }));
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void backElytraKeepsChestArmorAndNativeGliding(GameTestHelper helper) {
        ServerPlayer player = player(helper, "TrinketsWing");
        try {
            ItemStack armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
            player.setItemSlot(EquipmentSlot.CHEST, armor);
            equip(helper, player, "chest", "back", new ItemStack(ModItems.infinity_elytra.get()));
            BlockPos above = helper.absolutePos(new BlockPos(4, 10, 4));
            player.setPos(above.getX() + 0.5, above.getY(), above.getZ() + 0.5);
            player.setOnGround(false);
            player.setDeltaMovement(0, -0.1, 0.2);
            helper.assertTrue(player.tryToStartFallFlying(), "native glide takeoff must use the worn back elytra");
            for (int tick = 0; tick < 12; tick++) player.doTick();
            helper.assertTrue(player.isFallFlying(), "native living-entity update must retain accessory glide flight");
            helper.assertTrue(player.getItemBySlot(EquipmentSlot.CHEST) == armor,
                    "accessory gliding must not temporarily replace or discard chest armor");
            helper.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying,
                    "elytra gliding must not grant creative flight");
            helper.succeed();
        } finally {
            remove(player);
        }
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void wornTotemSurvivesLethalDamageAndConsumesDurability(GameTestHelper helper) {
        ServerPlayer player = player(helper, "TrinketsTotem");
        try {
            ItemStack totem = new ItemStack(ModItems.infinity_totem.get());
            equip(helper, player, "charm", "charm", totem);
            player.setOnGround(true);
            // Newly joined players have 60 ticks of vanilla damage immunity.
            for (int tick = 0; tick < 60; tick++) player.tick();
            player.removeAllEffects();
            player.setAbsorptionAmount(0);
            player.invulnerableTime = 0;
            player.setHealth(2);
            helper.assertTrue(player.hurt(player.damageSources().generic(), 1000),
                    "lethal damage must enter the actual native death-protection path");
            helper.assertTrue(player.isAlive() && player.getHealth() == player.getMaxHealth(),
                    "a worn totem must prevent native lethal damage");
            helper.assertTrue(totem.getDamageValue() == 1 && totem.getCount() == 1,
                    "first rescue must consume one durability on the actual worn stack");
            helper.assertTrue(player.hasEffect(MobEffects.REGENERATION), "totem rescue must apply its regeneration");
            helper.succeed();
        } finally {
            remove(player);
        }
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void wornShovelRefreshesAndUnequipExpiresPassiveEffects(GameTestHelper helper) {
        ServerPlayer player = player(helper, "TrinketsShovel");
        try {
            TrinketInventory back = equip(helper, player, "chest", "back", new ItemStack(ModItems.crystal_shovel.get()));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200));
            player.doTick();
            helper.assertTrue(player.getEffect(MobEffects.DIG_SPEED) != null
                            && player.getEffect(MobEffects.DIG_SPEED).getAmplifier() == 2,
                    "worn crystal shovel must provide Haste III");
            helper.assertTrue(player.getEffect(MobEffects.MOVEMENT_SPEED) != null
                            && player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 2,
                    "worn crystal shovel must provide Speed III");
            helper.assertTrue(!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && !player.hasEffect(MobEffects.DIG_SLOWDOWN),
                    "worn crystal shovel must remove both slow effects");
            back.setItem(0, ItemStack.EMPTY);
            for (int tick = 0; tick < 6; tick++) player.doTick();
            helper.assertTrue(!player.hasEffect(MobEffects.DIG_SPEED) && !player.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "unequipping must stop passive refresh without leaving permanent effects");
            helper.succeed();
        } finally {
            remove(player);
        }
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = NativeTestPlayers.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        helper.getLevel().addNewPlayer(player);
        return player;
    }

    private static TrinketInventory equip(GameTestHelper helper, ServerPlayer player, String group, String slot, ItemStack stack) {
        var component = TrinketsApi.getTrinketComponent(player).orElseThrow();
        var inventories = component.getInventory().get(group);
        helper.assertTrue(inventories != null && inventories.containsKey(slot), "player accessory slot must exist: " + group + "/" + slot);
        TrinketInventory inventory = inventories.get(slot);
        SlotReference reference = new SlotReference(inventory, 0);
        helper.assertTrue(TrinketsApi.evaluatePredicateSet(inventory.getSlotType().getValidatorPredicates(), stack, reference, player)
                        && TrinketsApi.getTrinket(stack.getItem()).canEquip(stack, reference, player),
                "actual accessory validators must accept " + stack + " in " + group + "/" + slot);
        inventory.setItem(0, stack);
        return inventory;
    }

    private static void remove(ServerPlayer player) {
        if (!player.isRemoved()) player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }
}
