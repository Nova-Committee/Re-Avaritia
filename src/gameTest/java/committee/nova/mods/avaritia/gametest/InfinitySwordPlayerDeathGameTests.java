package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

import java.util.UUID;

/** Player victims must execute the native ServerPlayer death and respawn lifecycle. */
public final class InfinitySwordPlayerDeathGameTests {
    @GameTest(template = "avaritia:portable_ui_empty")
    public static void swordDeathRunsNativeLifecycleAndRespawns(GameTestHelper helper) {
        checkDeath(helper);
    }

    @GameTest(template = "avaritia:portable_ui_empty")
    public static void finiteSwordPreservesMobLoot(GameTestHelper helper) {
        checkMobLoot(helper, false);
    }

    @GameTest(template = "avaritia:portable_ui_empty")
    public static void endlessSwordPreservesMobLoot(GameTestHelper helper) {
        checkMobLoot(helper, true);
    }

    private static void checkMobLoot(GameTestHelper helper, boolean endless) {
        ServerLevel level = helper.getLevel();
        ServerPlayer attacker = createPlayer(helper, "LootAttacker", new BlockPos(1, 2, 1));
        var victim = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(4, 2, 4));
        victim.setNoAi(true);
        ItemStack stack = new ItemStack(ModItems.infinity_sword.get());
        attacker.setItemSlot(EquipmentSlot.MAINHAND, stack);
        boolean previousEndless = ModConfig.isSwordAttackEndless.get();
        try {
            ModConfig.isSwordAttackEndless.set(endless);
            InfinitySwordItem sword = (InfinitySwordItem) stack.getItem();
            sword.onLeftClickEntity(stack, attacker, victim);
            helper.assertTrue(victim.dead && victim.getHealth() == 0.0F, "mob must reach terminal death");
            int beef = countBeef(level, victim);
            helper.assertTrue(beef > 0, "native cow loot must reach the world");
            sword.onLeftClickEntity(stack, attacker, victim);
            helper.assertTrue(countBeef(level, victim) == beef, "repeat attacks must not duplicate mob loot");
            helper.succeed();
        } finally {
            ModConfig.isSwordAttackEndless.set(previousEndless);
            level.removePlayerImmediately(attacker, Entity.RemovalReason.DISCARDED);
        }
    }

    private static int countBeef(ServerLevel level, Entity victim) {
        return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        victim.getBoundingBox().inflate(2.0D), item -> item.getItem().is(Items.BEEF))
                .stream().mapToInt(item -> item.getItem().getCount()).sum();
    }

    private static void checkDeath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer attacker = createPlayer(helper, "SwordAttacker", new BlockPos(1, 2, 1));
        ServerPlayer victim = createPlayer(helper, "SwordVictim", new BlockPos(4, 2, 4));
        CaptureListener listener = (CaptureListener) victim.connection;
        ItemStack stack = new ItemStack(ModItems.infinity_sword.get());
        attacker.setItemSlot(EquipmentSlot.MAINHAND, stack);
        victim.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        boolean previousEndless = ModConfig.isSwordAttackEndless.get();
        boolean previousKeep = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        int deathsBefore = victim.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int killsBefore = attacker.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS));
        try {
            ModConfig.isSwordAttackEndless.set(true);
            level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, level.getServer());
            InfinitySwordItem sword = (InfinitySwordItem) stack.getItem();
            sword.onLeftClickEntity(stack, attacker, victim);
            helper.assertTrue(victim.dead && victim.getHealth() == 0.0F, "sword death remains terminal");
            helper.assertTrue(listener.deathPackets == 1, "native death must send one player combat-kill packet");
            helper.assertTrue(victim.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) == deathsBefore + 1,
                    "native death statistic must increment once");
            helper.assertTrue(attacker.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)) == killsBefore + 1,
                    "native player-kill credit must increment once");
            helper.assertTrue(victim.getLastDeathLocation().isPresent(), "native death location must be recorded");
            helper.assertTrue(victim.getInventory().countItem(Items.DIAMOND) == 0, "death must drop inventory");
            sword.onLeftClickEntity(stack, attacker, victim);
            helper.assertTrue(listener.deathPackets == 1,
                    "repeat attacks on the same dead player must not repeat death callbacks");
            listener.handleClientCommand(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            ServerPlayer respawned = listener.player;
            helper.assertTrue(respawned != victim && respawned.getUUID().equals(victim.getUUID()),
                    "native respawn must replace the player instance while retaining identity");
            helper.assertTrue(respawned.isAlive() && !respawned.dead && respawned.getHealth() == respawned.getMaxHealth(),
                    "respawned player must be alive at full health");
            helper.assertTrue(listener.respawnPackets == 1, "native respawn must emit one respawn packet");
            helper.assertTrue(level.getServer().getPlayerList().getPlayer(respawned.getUUID()) == respawned,
                    "native respawn must register the replacement player");
            helper.succeed();
        } finally {
            ModConfig.isSwordAttackEndless.set(previousEndless);
            level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(previousKeep, level.getServer());
            level.removePlayerImmediately(attacker, Entity.RemovalReason.DISCARDED);
            if (listener.player != victim) {
                level.getServer().getPlayerList().remove(listener.player);
            } else {
                level.removePlayerImmediately(victim, Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, String name, BlockPos relativePos) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), name));
        new CaptureListener(level, player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(relativePos);
        player.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        level.addNewPlayer(player);
        return player;
    }

    private static final class CaptureListener extends ServerGamePacketListenerImpl {
        private int deathPackets;
        private int respawnPackets;

        private CaptureListener(ServerLevel level, ServerPlayer player) {
            super(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player);
        }

        @Override
        public void send(Packet<?> packet) {
            if (packet instanceof ClientboundPlayerCombatKillPacket) deathPackets++;
            if (packet instanceof ClientboundRespawnPacket) respawnPackets++;
        }

        @Override
        public void send(Packet<?> packet, PacketSendListener callback) {
            send(packet);
        }
    }
}
