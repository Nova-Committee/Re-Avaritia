package committee.nova.mods.avaritia.common.item.misc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinityElytraBehaviorTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/avaritia.mixins.json");
    private static final Path ELYTRA_TEXTURE = Path.of(
            "src/main/resources/assets/avaritia/textures/entity/infinity_elytra.png");
    private static final Path CURIOS_PLAYER = Path.of(
            "src/main/resources/data/avaritia/curios/entities/player.json");
    private static final Path CURIOS_BACK_TAG = Path.of(
            "src/generated/resources/data/curios/tags/item/back.json");

    @Test
    void infinityElytraFlightKeepsInfinityDamageAndFallProtectionContracts() {
        assertAll(
                contains("common/item/misc/InfinityElytraItem.java",
                        "target.hurt(ModDamageTypes.source(entity), ModConfig.infinityElytraFlyingRangeDamage.get().floatValue())"),
                contains("common/item/misc/InfinityElytraItem.java", "entity.resetFallDistance()"),
                contains("common/item/misc/InfinityElytraItem.java", "canDamageInFlight(entity, target)"),
                contains("init/handler/InfinityHandler.java", "onInfinityElytraFall(LivingFallEvent event)"),
                contains("init/handler/InfinityHandler.java",
                        "(ToolUtils.isInfinite(player) || isUsingInfinityElytra(player)) && !damageSource.is(ModDamageTypes.INFINITY)"),
                contains("init/handler/InfinityHandler.java",
                        "return isWearingInfinityElytra(player) && (player.isFallFlying() || !player.onGround())")
        );
    }

    @Test
    void infinityElytraUsesDedicatedWornTextureChain() throws IOException {
        String mixinConfig = compact(Files.readString(MIXIN_CONFIG));

        assertAll(
                contains("common/item/misc/InfinityElytraItem.java", ".setAsset(EquipmentAssets.ELYTRA)"),
                contains("client/AvaritiaClient.java", "INFINITY_ELYTRA_EXTENSIONS = new IClientItemExtensions()"),
                contains("client/AvaritiaClient.java",
                        "type == EquipmentClientInfo.LayerType.WINGS ? Res.INFINITY_ELYTRA : fallback"),
                contains("client/AvaritiaClient.java",
                        "event.registerItem(INFINITY_ELYTRA_EXTENSIONS, ModItems.infinity_elytra.get())"),
                contains("Res.java", "textures/entity/infinity_elytra.png"),
                () -> assertFalse(mixinConfig.contains("client.PlayerRendererMixin"),
                        "the vanilla wings layer must be the only elytra model submitter"),
                () -> assertFalse(Files.exists(MAIN_SOURCES.resolve("client/render/entity/InfinityElytraLayer.java")),
                        "a second custom wings layer would duplicate the vanilla model"),
                () -> assertTrue(Files.isRegularFile(ELYTRA_TEXTURE), "infinity elytra texture must exist")
        );
    }

    @Test
    void infinityElytraWorksFromCuriosBackSlotAcrossClientAndServer() throws IOException {
        String curiosPlayer = compact(Files.readString(CURIOS_PLAYER));
        String curiosBackTag = compact(Files.readString(CURIOS_BACK_TAG));

        assertAll(
                contains("util/InfinityElytraUtils.java",
                        "CuriosTools.getFirstItemFromCuriosSlot(player, BACK_SLOT, InfinityElytraUtils::isInfinityElytra)"),
                contains("client/AvaritiaClient.java",
                        "InfinityElytraUtils.hasInfinityElytraEquipped(player)"),
                contains("common/net/C2SElytraSpeedUpPacket.java",
                        "InfinityElytraUtils.hasInfinityElytraEquipped(player)"),
                contains("common/net/C2SElytraSpeedUpPacket.java",
                        "InfinityElytraUtils.prepareCuriosFallbackTakeoff(player)"),
                contains("common/net/C2SElytraSpeedUpPacket.java",
                        "return InfinityElytraUtils.startCuriosFallbackFallFlying(player)"),
                contains("util/InfinityElytraUtils.java",
                        "!player.isPassenger() && !player.isInWater() && !player.onClimbable()"),
                contains("util/InfinityElytraUtils.java",
                        "if (!player.isFallFlying()) { activateCuriosFallbackFallFlying(player); }"),
                contains("util/InfinityElytraUtils.java", "player.startFallFlying()"),
                contains("util/InfinityElytraUtils.java", "return player.isFallFlying()"),
                contains("init/handler/InfinityHandler.java",
                        "return InfinityElytraUtils.hasInfinityElytraEquipped(player)"),
                contains("init/handler/InfinityHandler.java",
                        "InfinityElytraUtils.updateCuriosFallbackFallFlying(player)"),
                () -> assertTrue(curiosPlayer.contains("\"minecraft:player\"")),
                () -> assertTrue(curiosPlayer.contains("\"back\"")),
                () -> assertTrue(curiosBackTag.contains("\"avaritia:infinity_elytra\""))
        );
    }

    private static Executable contains(String relativePath, String expected) {
        return () -> assertTrue(
                compact(read(relativePath)).contains(compact(expected)),
                () -> relativePath + " should keep infinity elytra behavior contract: " + expected
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
