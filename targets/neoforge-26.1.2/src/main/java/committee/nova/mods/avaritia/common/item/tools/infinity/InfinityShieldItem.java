package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class InfinityShieldItem extends ShieldItem implements ISwitchable, IUndamageable {
    public InfinityShieldItem() {
        super((ModItems.properties())
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant()
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(
                        DataComponents.BLOCKS_ATTACKS,
                        context -> new BlocksAttacks(
                                0.01F,
                                0F,
                                List.of(new BlocksAttacks.DamageReduction(180.0F, Optional.empty(), 9999.0F, 1.0F)),
                                new BlocksAttacks.ItemDamageFunction(9999.0F, 0F, 0.01F),
                                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                Optional.of(SoundEvents.SHIELD_BLOCK),
                                Optional.of(SoundEvents.SHIELD_BREAK)
                        )
                )
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                .component(DataComponents.USE_EFFECTS, new UseEffects(true, false, 1.0F))
        );

    }
    public static final List<String> MODES = List.of(
            "infinity_shield_normal",
            "infinity_shield_defending",
            "infinity_shield_definite_defending",
            "infinity_shield_float"
    );
    public static final int MODE_NORMAL = 0;
    public static final int MODE_DEFENDING = 1;
    public static final int MODE_DEFINITE_DEFENDING = 2;
    public static final int MODE_FLOAT = 3;
    public static int getShieldMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        CompoundTag root = customData.copyTag();
        CompoundTag modeTag = root.getCompound("mode").orElse(null);
        if (modeTag == null) {
            return 0;
        }
        for (int i = 0; i < MODES.size(); i++) {
            if (modeTag.getBoolean(MODES.get(i)).orElse(false)) {
                return i;
            }
        }
        return 0;
    }
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            cycleMode(level, player, hand, MODES);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    public static boolean isDefendingMode(ItemStack stack) {
        return getShieldMode(stack) == MODE_DEFENDING;
    }

    public static boolean isDefiniteDefendingMode(ItemStack stack) {
        return getShieldMode(stack) == MODE_DEFINITE_DEFENDING;
    }
    public static boolean isFloatMode(ItemStack stack) {
        return getShieldMode(stack) == MODE_FLOAT;
    }
//    @Override
//    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//        consumer.accept(new IClientItemExtensions() {
//            private InfinityShieldRender renderer;
//
//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                if (renderer == null) {
//                    renderer = new InfinityShieldRender(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
//                }
//                return renderer;
//            }
//        });
//    }

}
