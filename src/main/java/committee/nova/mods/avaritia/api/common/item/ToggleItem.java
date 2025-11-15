package committee.nova.mods.avaritia.api.common.item;


import committee.nova.mods.avaritia.api.iface.item.mode.IModeChanger;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @project: Avaritia
 * @author: cnlimiter
 * @createTime: 2025/5/24 20:23
 * @apiNote:
 */
public class ToggleItem extends BaseItem implements IModeChanger<Boolean> {
    public ToggleItem() {
        super(props -> props.component(ModDataComponents.ACTIVE, false));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public Boolean getMode(@NotNull ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ACTIVE, false);
    }

    @Override
    public boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
        boolean isActive = getMode(stack);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), isActive ? ModSounds.MODE.get() : ModSounds.HEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.set(ModDataComponents.ACTIVE, !isActive);
        return true;
    }
}
