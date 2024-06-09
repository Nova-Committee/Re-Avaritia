package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * InfinityHorseArmorItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 20:04
 */
public class InfinityHorseArmorItem extends HorseArmorItem {
    private static final String TEX_PATH = "textures/entity/infinity_horse_armor.png";

    public InfinityHorseArmorItem(Properties pProperties) {
        super(Integer.MAX_VALUE, new ResourceLocation(Static.MOD_ID, TEX_PATH), pProperties);
    }
    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }


}
