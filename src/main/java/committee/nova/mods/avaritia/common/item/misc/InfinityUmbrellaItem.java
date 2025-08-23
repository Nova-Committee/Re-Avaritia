package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.IFourModeSwitchable;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter,cu6
 * @CreateTime: 2025/08/23
 * @Description: Now,We Did it,Four Modes
 */
public class InfinityUmbrellaItem extends ResourceItem implements IFourModeSwitchable {

    public static final String MODE_KEY = "srs";

    private static final int MODE_NORMAL = 0;
    private static final int MODE_SUN = 1;
    private static final int MODE_RAIN = 2;
    private static final int MODE_STORM = 3;


    public InfinityUmbrellaItem() {
        super(ModRarities.COSMIC, "infinity_umbrella", true, new Properties().stacksTo(1));
    }


    private void onUse(Level world, Player player, ItemStack stack) {

        if (world.isClientSide) return;


        int currentMode = IFourModeSwitchable.getMode(stack, MODE_KEY);

        switch (currentMode) {
            case MODE_NORMAL:
                //0
                break;
            case MODE_SUN:
                //1
                break;
            case MODE_RAIN:
                //2
                break;
            case MODE_STORM:
                //3
                break;
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            cycleMode(world, player, hand, MODE_KEY, new String[]{
                    "tooltip.avaritia.infinity_umbrella.normal",
                    "tooltip.avaritia.infinity_umbrella.sun",
                    "tooltip.avaritia.infinity_umbrella.rain",
                    "tooltip.avaritia.infinity_umbrella.storm"
            });
            return InteractionResultHolder.success(stack);
        }
        onUse(world, player, stack);
        return super.use(world, player, hand);
    }

}
