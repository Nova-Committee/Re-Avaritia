package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfinityShieldItem extends ShieldItem implements ISwitchable, IUndamageable {
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

    public InfinityShieldItem() {
        super((new Properties())
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant());
    }

    /**
     * Read-only mode index from ISwitchable custom_data.mode booleans.
     * Missing or empty mode data is {@link #MODE_NORMAL}; this method does not write NBT.
     */
    public static int getShieldMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return MODE_NORMAL;
        }
        CompoundTag root = customData.copyTag();
        if (!root.contains("mode")) {
            return MODE_NORMAL;
        }
        CompoundTag modeTag = root.getCompound("mode");
        for (int i = 0; i < MODES.size(); i++) {
            String mode = MODES.get(i);
            if (modeTag.contains(mode) && modeTag.getBoolean(mode)) {
                return i;
            }
        }
        return MODE_NORMAL;
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

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleMode(level, player, hand, MODES);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }
}
