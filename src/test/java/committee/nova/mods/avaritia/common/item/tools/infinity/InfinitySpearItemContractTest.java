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

    @Test
    void longRangeModeCyclesExplicitlyWithoutLeakingLunge() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains(
                        "List.of(MODE_NORMAL,MODE_LUNGE,MODE_LONG_RANGE)")),
                () -> assertTrue(source.contains("cycleMode(level,player,hand,MODES)")),
                () -> assertTrue(source.contains(
                        "enchantmentHolder.is(Enchantments.LUNGE)&&isActive((ItemStack)stack,MODE_LUNGE)")),
                () -> assertTrue(source.contains(
                        "if(!level.isClientSide()&&!isActive(stack,MODE_LONG_RANGE)){stack.remove"))
        );
    }

    @Test
    void lockedTargetIsPersistentUnlimitedAndServerAuthoritative() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String components = compact(Files.readString(COMPONENT_SOURCE));

        assertAll(
                () -> assertTrue(spear.contains(
                        "if(level.isClientSide()){returnInteractionResult.SUCCESS;}")),
                () -> assertTrue(spear.contains(
                        "SpearThrustUtils.movePlayerToTarget(level,player,target)")),
                () -> assertTrue(spear.contains(
                        "player.stabAttack(hand.asEquipmentSlot(),target,damage,true,false,false)")),
                () -> assertTrue(spear.contains(
                        "addTicketAndLoadWithRadius(TicketType.PORTAL,requestedTarget.lastKnownChunk(),0)")),
                () -> assertTrue(spear.contains(
                        "currentTarget==null||!currentTarget.matches(target)")),
                () -> assertTrue(components.contains("persistent(SpearTargetReference.CODEC)")),
                () -> assertTrue(components.contains("networkSynchronized(SpearTargetReference.STREAM_CODEC)")),
                () -> assertFalse(spear.contains("remainingThrusts")),
                () -> assertFalse(spear.contains("consumeThrust"))
        );
    }

    @Test
    void automaticTargetingRandomizesOnlyAmongTheNearestEligibleTargets() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains("AUTO_TARGET_RANGE=128.0D")),
                () -> assertTrue(source.contains("inflate(AUTO_TARGET_RANGE)")),
                () -> assertTrue(source.contains(
                        ".sorted(Comparator.comparingDouble(player::distanceToSqr))")),
                () -> assertTrue(source.contains(".limit(AUTO_TARGET_POOL_SIZE)")),
                () -> assertTrue(source.contains(
                        "level.getRandom().nextInt(nearestTargets.size())")),
                () -> assertTrue(source.contains("!player.isAlliedTo(target)")),
                () -> assertTrue(source.contains("!(targetinstanceofArmorStand)")),
                () -> assertTrue(source.contains("!target.isSpectator()")),
                () -> assertTrue(source.contains("targetPlayer.isCreative()")),
                () -> assertTrue(source.contains("target!=player&&target.isAlive()&&!target.isRemoved()"))
        );
    }

    @Test
    void asynchronousRetryRevalidatesModeStackAndTargetIdentity() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains("player.getItemInHand(hand)!=stack")),
                () -> assertTrue(source.contains("!ISwitchable.isMode(stack,MODE_LONG_RANGE)")),
                () -> assertTrue(source.contains(
                        "!currentTarget.targetId().equals(requestedTarget.targetId())")),
                () -> assertFalse(source.contains("ProjectileUtil")),
                () -> assertFalse(source.contains("ClipContext"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
