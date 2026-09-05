package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.client.render.item.InfinityShieldRender;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

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
        super((new Item.Properties())
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant());
    }

    /** Missing mode data means normal; rendering and event queries must not mutate stack NBT. */
    public static int getShieldMode(ItemStack stack) {
        CompoundTag modeTag = stack.getTagElement("mode");
        if (modeTag != null) {
            for (int i = 0; i < MODES.size(); i++) {
                if (modeTag.getBoolean(MODES.get(i))) {
                    return i;
                }
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

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private InfinityShieldRender renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new InfinityShieldRender(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        });
    }

}
