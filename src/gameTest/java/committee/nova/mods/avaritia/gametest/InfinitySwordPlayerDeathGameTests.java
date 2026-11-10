package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Consumer;

/** Player victims must execute ServerPlayer death and respawn, including when a death hook heals them. */
@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinitySwordPlayerDeathGameTests {
    @GameTest(template = "portable_ui_empty")
    public static void swordDeathRunsNativeLifecycleAndRespawns(GameTestHelper helper) {
        checkDeath(helper, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void canceledSwordDeathRunsNativeLifecycleOnce(GameTestHelper helper) {
        checkDeath(helper, true);
    }

    @GameTest(template = "portable_ui_empty")
    public static void ordinaryPlayerDeathRemainsCancelable(GameTestHelper helper) {
        ServerPlayer victim = createPlayer(helper, "DeathCancel", new BlockPos(4, 2, 4));
        CaptureListener listener = (CaptureListener) victim.connection;
        Consumer<LivingDeathEvent> cancel = event -> {
            if (event.getEntity() == victim) {
                event.setCanceled(true);
                victim.setHealth(victim.getMaxHealth());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, cancel);
        try {
            victim.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
            int deathsBefore = victim.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
            victim.setHealth(0.0F);
            victim.die(victim.damageSources().generic());
            helper.assertTrue(victim.isAlive() && !victim.dead, "ordinary death cancellation must preserve the player");
            helper.assertTrue(listener.deathPackets == 0, "canceled ordinary death must not send a death packet");
            helper.assertTrue(victim.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) == deathsBefore,
                    "canceled ordinary death must not increment the death statistic");
            helper.assertTrue(victim.getInventory().countItem(Items.DIAMOND) == 3,
                    "canceled ordinary death must not drop inventory");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
            victim.serverLevel().removePlayerImmediately(victim, Entity.RemovalReason.DISCARDED);
        }
    }

    private static void checkDeath(GameTestHelper helper, boolean cancelDeath) {
        ServerLevel level = helper.getLevel();
        ServerPlayer attacker = createPlayer(helper, "SwordAttacker", new BlockPos(1, 2, 1));
        ServerPlayer victim = createPlayer(helper, "SwordVictim", new BlockPos(4, 2, 4));
        CaptureListener listener = (CaptureListener) victim.connection;
        ItemStack stack = new ItemStack(ModItems.infinity_sword.get());
        attacker.setItemSlot(EquipmentSlot.MAINHAND, stack);
        victim.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        int[] deaths = {0};
        int[] drops = {0};
        Consumer<LivingDeathEvent> deathObserver = event -> {
            if (event.getEntity() == victim) {
                deaths[0]++;
                helper.assertTrue(event.getSource().getEntity() == attacker, "death must retain the killer");
                if (cancelDeath) {
                    event.setCanceled(true);
                    victim.setHealth(victim.getMaxHealth());
                }
            }
        };
        Consumer<LivingDropsEvent> dropObserver = event -> {
            if (event.getEntity() == victim) drops[0]++;
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, deathObserver);
        MinecraftForge.EVENT_BUS.addListener(dropObserver);
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
            helper.assertTrue(deaths[0] == 1 && drops[0] == 1, "death and inventory-drop hooks must run once");
            helper.assertTrue(victim.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) == deathsBefore + 1,
                    "native death statistic must increment once");
            helper.assertTrue(attacker.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)) == killsBefore + 1,
                    "native player-kill credit must increment once");
            helper.assertTrue(victim.getLastDeathLocation().isPresent(), "native death location must be recorded");
            helper.assertTrue(victim.getInventory().countItem(Items.DIAMOND) == 0, "death must drop inventory");
            sword.onLeftClickEntity(stack, attacker, victim);
            helper.assertTrue(deaths[0] == 1 && drops[0] == 1 && listener.deathPackets == 1,
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
            MinecraftForge.EVENT_BUS.unregister(deathObserver);
            MinecraftForge.EVENT_BUS.unregister(dropObserver);
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
