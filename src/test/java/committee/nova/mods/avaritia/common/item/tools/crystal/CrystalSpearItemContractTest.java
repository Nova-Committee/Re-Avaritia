package committee.nova.mods.avaritia.common.item.tools.crystal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalSpearItemContractTest {
    private static final Path SPEAR_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/item/tools/crystal/CrystalSpearItem.java");
    private static final Path COMPONENT_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/init/registry/ModDataComponents.java");

    @Test
    void lockedAttackIsServerAuthoritativeAndHasNoRangeOrSightGate() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains("if(level.isClientSide()){returnInteractionResult.SUCCESS;}")),
                () -> assertTrue(source.contains("booleanattacked=player.stabAttack(hand.asEquipmentSlot(),target,damage,true,false,false);")),
                () -> assertTrue(source.contains("target.level()!=level")),
                () -> assertTrue(source.contains("addTicketAndLoadWithRadius(TicketType.PORTAL,requestedTarget.lastKnownChunk(),0)")),
                () -> assertFalse(source.contains("distanceTo")),
                () -> assertFalse(source.contains("ProjectileUtil")),
                () -> assertFalse(source.contains("ClipContext"))
        );
    }

    @Test
    void sevenfoldModeCyclesExplicitlyAndOwnsTheTargetLock() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains(
                        "List.of(MODE_NORMAL,MODE_SHATTER,MODE_SEVENFOLD)")),
                () -> assertTrue(source.contains("cycleMode(level,player,hand,MODES)")),
                () -> assertTrue(source.contains(
                        "if(isActive(stack,MODE_SEVENFOLD)){CrystalSpearTargetcurrentTarget=")),
                () -> assertTrue(source.contains(
                        "if(!level.isClientSide()&&!isActive(stack,MODE_SEVENFOLD)){stack.remove")),
                () -> assertTrue(source.contains(
                        "tooltip.avaritia.tool.crystal_spear_sevenfold"))
        );
    }

    @Test
    void onlySuccessfulLockedAttacksConsumeTheFourteenUseBudget() throws IOException {
        String source = compact(Files.readString(SPEAR_SOURCE));

        assertAll(
                () -> assertTrue(source.contains("if(!attacked){returntrue;}")),
                () -> assertTrue(source.contains("currentTarget.consumeThrust()")),
                () -> assertTrue(source.contains(
                        "if(nextTarget==null){stack.remove(ModDataComponents.CRYSTAL_SPEAR_TARGET.get())")),
                () -> assertTrue(source.contains("message.avaritia.crystal_spear.exhausted"))
        );
    }

    @Test
    void firstHitStoresAReplaceablePersistentTargetReference() throws IOException {
        String spear = compact(Files.readString(SPEAR_SOURCE));
        String components = compact(Files.readString(COMPONENT_SOURCE));

        assertAll(
                () -> assertTrue(spear.contains("stack.set(ModDataComponents.CRYSTAL_SPEAR_TARGET.get(),CrystalSpearTarget.of(target));")),
                () -> assertTrue(spear.contains("currentTarget==null||!currentTarget.matches(target)")),
                () -> assertTrue(components.contains("persistent(CrystalSpearTarget.CODEC)")),
                () -> assertTrue(components.contains("networkSynchronized(CrystalSpearTarget.STREAM_CODEC)"))
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
