package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.init.registry.FuelBlockItem;
import committee.nova.mods.avaritia.common.item.resources.RefinedCoalItem;
import committee.nova.mods.avaritia.common.item.resources.StarFuelItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class GameplayFurnaceMixin {
    @Inject(method = "getFuel", at = @At("RETURN"))
    private static void avaritia$fuels(CallbackInfoReturnable<Map<Item, Integer>> cir) {
        Map<Item, Integer> fuel = cir.getReturnValue();
        fuel.put(ModItems.star_fuel.get(), StarFuelItem.BURN_TIME);
        fuel.put(ModItems.refined_coal.get(), RefinedCoalItem.BURN_TIME);
        Item starBlock = ModBlocks.star_fuel_block.get().asItem();
        Item coalBlock = ModBlocks.refined_coal_block.get().asItem();
        fuel.put(starBlock, ((FuelBlockItem) starBlock).burnTime());
        fuel.put(coalBlock, ((FuelBlockItem) coalBlock).burnTime());
    }
}
