package committee.nova.mods.avaritia.common.item.tools.infinity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinitySpearItemContractTest {
    private static final Path SPEAR_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/item/tools/infinity/InfinitySpearItem.java");
    private static final Path COMPONENT_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/init/registry/ModDataComponents.java");
    private static final Path THRUST_UTILS_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/item/tools/SpearThrustUtils.java");

    @Test
    void longRangeModeKeepsTheMostRecentMarkAcrossModeSwitches() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains("List.of(MODE_NORMAL,MODE_LUNGE,MODE_LONG_RANGE)")),
                () -> assertTrue(source.contains("cycleMode(level,player,hand,MODES)")),
                () -> assertTrue(source.contains(
                        "enchantmentHolder.is(Enchantments.LUNGE)&&isActive((ItemStack)stack,MODE_LUNGE)")),
                () -> assertFalse(source.contains(
                        "if(!level.isClientSide()&&!isActive(stack,MODE_LONG_RANGE)){stack.remove"))
        );
    }

    @Test
    void everyLivingHitTransfersAndRefreshesThePersistentMarkedTarget() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String components = compact(Files.readString(COMPONENT_SOURCE));

        assertAll(
                () -> assertTrue(spear.contains("booleannewlyMarked=!SpearMarkUtils.isMarkedBy(target,player)")),
                () -> assertTrue(spear.contains("SpearMarkmark=SpearMarkUtils.apply(target,player)")),
                () -> assertTrue(spear.contains(
                        "SpearTargetReference.of(target,player.getUUID(),mark.expiresAt())")),
                () -> assertTrue(spear.contains("message.avaritia.infinity_spear.marked")),
                () -> assertTrue(components.contains("persistent(SpearTargetReference.CODEC)")),
                () -> assertTrue(components.contains("networkSynchronized(SpearTargetReference.STREAM_CODEC)")),
                () -> assertFalse(spear.contains("remainingThrusts")),
                () -> assertFalse(spear.contains("consumeThrust"))
        );
    }

    @Test
    void markedTargetHasPriorityAndOtherwiseSelectionIsRandomAmongNearestEligibleTargets() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String movement = compact(Files.readString(THRUST_UTILS_SOURCE));
        int markLookup = spear.indexOf("SpearTargetReferencemarkedTarget=stack.get");
        int randomLookup = spear.indexOf("SpearThrustUtils.selectRandomTarget", markLookup);

        assertAll(
                () -> assertTrue(markLookup >= 0 && randomLookup > markLookup),
                () -> assertTrue(spear.contains("markedTarget.isOwnedBy(player.getUUID(),level.getGameTime())")),
                () -> assertTrue(spear.contains("SpearMarkUtils.isMarkedBy(target,player)")),
                () -> assertTrue(movement.contains(".sorted(Comparator.comparingDouble(player::distanceToSqr))")),
                () -> assertTrue(movement.contains(".limit(poolSize)")),
                () -> assertTrue(movement.contains("level.getRandom().nextInt(nearestTargets.size())")),
                () -> assertTrue(movement.contains("!player.isAlliedTo(target)")),
                () -> assertTrue(movement.contains("!(targetinstanceofArmorStand)")),
                () -> assertTrue(movement.contains("targetPlayer.isCreative()"))
        );
    }

    @Test
    void asynchronousRetryRevalidatesModeStackOwnerExpiryAndLiveMark() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains("player.getItemInHand(hand)!=stack")),
                () -> assertTrue(source.contains("!ISwitchable.isMode(stack,MODE_LONG_RANGE)")),
                () -> assertTrue(source.contains("!currentTarget.equals(requestedTarget)")),
                () -> assertTrue(source.contains("!currentTarget.isOwnedBy(player.getUUID(),level.getGameTime())")),
                () -> assertTrue(source.contains("!SpearMarkUtils.isMarkedBy(target,player)")),
                () -> assertFalse(source.contains("ProjectileUtil")),
                () -> assertFalse(source.contains("ClipContext"))
        );
    }

    @Test
    void remoteMultiplierAlsoCoversFiniteInfinityBonusDamage() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertTrue(source.contains(
                "ModToolTiers.INFINITY.attackDamageBonus()*SpearThrustUtils.remoteDamageMultiplier(player,target)"));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
