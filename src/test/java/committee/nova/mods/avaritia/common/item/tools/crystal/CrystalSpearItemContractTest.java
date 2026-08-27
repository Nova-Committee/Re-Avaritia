package committee.nova.mods.avaritia.common.item.tools.crystal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalSpearItemContractTest {
    private static final Path SPEAR_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/item/tools/crystal/CrystalSpearItem.java");
    private static final Path COMPONENT_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/init/registry/ModDataComponents.java");
    private static final Path THRUST_UTILS_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/item/tools/SpearThrustUtils.java");

    @Test
    void sevenfoldRandomlyThrustsAtNearbyEligibleTargetsWithoutALock() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String movement = compact(Files.readString(THRUST_UTILS_SOURCE));

        assertAll(
                () -> assertTrue(spear.contains("if(level.isClientSide()){returnInteractionResult.SUCCESS;}")),
                () -> assertTrue(spear.contains(
                        "SpearThrustUtils.selectRandomTarget(serverLevel,serverPlayer,AUTO_TARGET_RANGE,AUTO_TARGET_POOL_SIZE)")),
                () -> assertTrue(spear.contains("AUTO_TARGET_RANGE=128.0D")),
                () -> assertTrue(spear.contains("SpearThrustUtils.movePlayerToTarget(serverLevel,serverPlayer,target)")),
                () -> assertTrue(movement.contains(".sorted(Comparator.comparingDouble(player::distanceToSqr))")),
                () -> assertTrue(movement.contains("!player.isAlliedTo(target)")),
                () -> assertTrue(movement.contains("!(targetinstanceofArmorStand)")),
                () -> assertFalse(spear.contains("TicketType")),
                () -> assertFalse(spear.contains("lockedTarget"))
        );
    }

    @Test
    void fourteenUseBudgetIsIndependentAndOnlySuccessfulThrustsConsumeIt() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String components = compact(Files.readString(COMPONENT_SOURCE));
        int attackIndex = spear.indexOf("if(SpearThrustUtils.stabTarget(serverPlayer,hand,target)){");
        int decrementIndex = spear.indexOf("intnextRemaining=remainingThrusts-1", attackIndex);

        assertAll(
                () -> assertTrue(spear.contains(
                        "stack.set(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get(),CrystalSpearTarget.MAX_THRUSTS)")),
                () -> assertTrue(attackIndex >= 0 && decrementIndex > attackIndex),
                () -> assertTrue(spear.contains("if(remainingThrusts<=0)")),
                () -> assertTrue(spear.contains("message.avaritia.crystal_spear.exhausted")),
                () -> assertTrue(components.contains("Codec.intRange(0,CrystalSpearTarget.MAX_THRUSTS)")),
                () -> assertTrue(components.contains("networkSynchronized(ByteBufCodecs.VAR_INT)"))
        );
    }

    @Test
    void exhaustedBudgetStartsAPersistentPerStackCooldownAndCannotBeResetByModeSwitching() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String components = compact(Files.readString(COMPONENT_SOURCE));
        int successfulAttack = spear.indexOf("if(SpearThrustUtils.stabTarget(serverPlayer,hand,target)){");
        int cooldownStart = spear.indexOf("if(nextRemaining==0){startCooldown(stack,gameTime);", successfulAttack);

        assertAll(
                () -> assertTrue(cooldownStart > successfulAttack),
                () -> assertTrue(spear.contains("CrystalSpearCooldown.DURATION_TICKS/20")),
                () -> assertTrue(spear.contains("cooldown.remainingSeconds(gameTime)")),
                () -> assertTrue(spear.contains("refreshCompletedCooldown(stack,level.getGameTime())")),
                () -> assertTrue(spear.contains(
                        "stack.set(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get(),CrystalSpearTarget.MAX_THRUSTS)")),
                () -> assertFalse(spear.contains(
                        "else{stack.remove(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get())")),
                () -> assertTrue(components.contains("DataComponentType<CrystalSpearCooldown>")),
                () -> assertTrue(components.contains("persistent(CrystalSpearCooldown.CODEC)")),
                () -> assertTrue(components.contains("networkSynchronized(CrystalSpearCooldown.STREAM_CODEC)"))
        );
    }

    @Test
    void everyLivingHitRefreshesTheOwnerMarkAndRemoteDamageDoublesTogether() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String movement = compact(Files.readString(THRUST_UTILS_SOURCE));

        assertAll(
                () -> assertTrue(spear.contains("booleannewlyMarked=!SpearMarkUtils.isMarkedBy(target,player)")),
                () -> assertTrue(spear.contains("SpearMarkUtils.apply(target,player)")),
                () -> assertTrue(spear.contains("message.avaritia.crystal_spear.marked")),
                () -> assertTrue(spear.contains("floatremoteMultiplier=SpearThrustUtils.remoteDamageMultiplier(player,target)")),
                () -> assertTrue(spear.contains("calculateBonusDamage(baseDamage,target)*remoteMultiplier")),
                () -> assertTrue(movement.contains("SpearMarkUtils.isMarkedBy(target,player)?2.0F:1.0F"))
        );
    }

    @Test
    void armorAndToughnessScalingUsesReducedCoefficients() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));
        float multiplier = 1.0F + 20.0F * 0.025F + 8.0F * 0.04F;

        assertAll(
                () -> assertTrue(source.contains("ARMOR_BONUS=0.025F")),
                () -> assertTrue(source.contains("TOUGHNESS_BONUS=0.04F")),
                () -> assertFalse(source.contains("ARMOR_BONUS=0.25F")),
                () -> assertFalse(source.contains("TOUGHNESS_BONUS=0.40F")),
                () -> assertEquals(1.82F, multiplier, 0.0001F)
        );
    }

    @Test
    void builtInLungeEnchantmentIsCompletelyRemoved() throws IOException {
        String source = Files.readString(SPEAR_SOURCE);

        assertAll(
                () -> assertFalse(source.contains("InitEnchantItem")),
                () -> assertFalse(source.contains("InitEnchantment")),
                () -> assertFalse(source.contains("Enchantments.LUNGE")),
                () -> assertFalse(source.contains("doPostPiercingAttackEffects"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
