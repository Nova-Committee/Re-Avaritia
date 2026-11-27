package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.util.UUID;
import java.util.function.Consumer;

/** Substitute fixture: real death cancellation/healing, not a Draconic Evolution compatibility test. */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class InfinityPlayerDeathGameTests {
    private static final String[] TESTS = {"player_canceled", "player_normal", "ordinary_cancelable", "cow_canceled", "dragon_canceled", "wither_canceled", "player_incoming_canceled", "player_zero_health_canceled", "wither_normal"};
    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        for (int i = 0; i < TESTS.length; i++) {
            final int scenario = i;
            event.register(Registries.TEST_FUNCTION, Const.rl("issue149_" + TESTS[i]), () -> h -> run(h, scenario));
        }
    }
    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(Const.rl("issue149"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            Identifier id = Const.rl("issue149_" + name);
            event.registerTest(id, new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id),
                    new TestData<>(env, Identifier.withDefaultNamespace("empty"), 100, 0, true)));
        }
    }
    private static ServerPlayer player(GameTestHelper h, String name) {
        ServerLevel level = h.getLevel();
        ServerPlayer p = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), name), ClientInformation.createDefault());
        new Capture(level, p);
        BlockPos pos = h.absolutePos(new BlockPos(2, 3, 2));
        p.setPos(pos.getX(), pos.getY(), pos.getZ());
        level.addNewPlayer(p);
        for (int i = 0; i < 60; i++) p.tick();
        return p;
    }
    private static void run(GameTestHelper h, int scenario) {
        ServerPlayer attacker = player(h, "I149Attacker");
        LivingEntity victim = switch (scenario) {
            case 3 -> h.spawn(EntityTypes.COW, new BlockPos(5, 3, 5));
            case 4 -> h.spawn(EntityTypes.ENDER_DRAGON, new BlockPos(5, 3, 5));
            case 5, 8 -> h.spawn(EntityTypes.WITHER, new BlockPos(5, 3, 5));
            default -> player(h, "I149Victim");
        };
        int[] events = {0};
        Consumer<LivingDeathEvent> cancel = e -> {
            if (e.getEntity() == victim) {
                events[0]++;
                if (scenario != 1 && scenario != 8) {
                    e.setCanceled(true);
                    if (scenario != 7) victim.setHealth(victim.getMaxHealth());
                }
            }
        };
        Consumer<net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent> incoming = e -> {
            if (scenario == 6 && e.getEntity() == victim) e.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(incoming);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, cancel);
        try {
            var source = ModDamageTypes.source(h.getLevel(), victim, attacker);
            int before = victim instanceof ServerPlayer p ? p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) : 0;
            if (victim instanceof ServerPlayer p) p.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
            if (scenario == 2) victim.hurtServer(h.getLevel(), source, Float.MAX_VALUE);
            else InfinityDamageUtils.forceKill(h.getLevel(), victim, source);
            if (scenario == 2) {
                h.assertTrue(events[0] == 1 && victim.isAlive(), "ordinary death remains canceled and healed");
                h.assertTrue(((Capture)((ServerPlayer)victim).connection).deaths == 0, "ordinary canceled death sends no packet");
            } else {
                String state = "scenario=" + TESTS[scenario] + " class=" + victim.getClass().getName() + " health=" + victim.getHealth() + " events=" + events[0];
                if (victim instanceof ServerPlayer p) {
                    state += " packets=" + ((Capture)p.connection).deaths + " deathDelta=" + (p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) - before) + " location=" + p.getLastDeathLocation();
                }
                Const.LOGGER.info("ISSUE149 {}", state);
                h.assertTrue(victim.isDeadOrDying(), "infinity death is terminal: " + state);
                h.assertTrue(events[0] == 1, "exactly one real death event: " + state);
                if (victim instanceof ServerPlayer p) {
                    h.assertTrue(((Capture)p.connection).deaths == 1, "native death packet missing: " + state);
                    h.assertTrue(p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) == before + 1, "death stat increments once");
                    h.assertTrue(p.getLastDeathLocation().isPresent(), "death location recorded");
                    h.assertTrue(p.getInventory().countItem(Items.DIAMOND) == 0, "player inventory drops");
                }
                InfinityDamageUtils.forceKill(h.getLevel(), victim, source);
                h.assertTrue(events[0] == 1, "repeat infinity hit must not repeat death event");
                if (victim instanceof ServerPlayer p) h.assertTrue(((Capture)p.connection).deaths == 1, "repeat hit must not duplicate death packet");
            }
            h.succeed();
        } finally {
            NeoForge.EVENT_BUS.unregister(cancel);
            NeoForge.EVENT_BUS.unregister(incoming);
            h.getLevel().removePlayerImmediately(attacker, Entity.RemovalReason.DISCARDED);
            if (victim instanceof ServerPlayer p) h.getLevel().removePlayerImmediately(p, Entity.RemovalReason.DISCARDED);
            else victim.discard();
        }
    }
    private static final class Capture extends ServerGamePacketListenerImpl {
        private int deaths;
        Capture(ServerLevel level, ServerPlayer p) {
            super(level.getServer(), new Connection(PacketFlow.SERVERBOUND), p, CommonListenerCookie.createInitial(p.getGameProfile(), false));
        }
        @Override public void send(Packet<?> packet) { if (packet instanceof ClientboundPlayerCombatKillPacket) deaths++; }
        @Override public void send(Packet<?> packet, ChannelFutureListener listener) { send(packet); }
    }
}
