package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class InfinityElytraItem extends ElytraItem implements ICurioItem {
    public InfinityElytraItem() {
        super(new Item.Properties()
                .rarity(ModRarities.COSMIC)
                .fireResistant()
                .stacksTo(1));
    }

    @Override
    public boolean canElytraFly(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return true;
    }


//    @Override
//    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
//        // 只在服务器/客户端都设置 NBT 无妨，会持久化
//
//    }
    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!stack.hasTag() || !stack.getTag().getBoolean("Unbreakable")) {
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
        }
    }
}
