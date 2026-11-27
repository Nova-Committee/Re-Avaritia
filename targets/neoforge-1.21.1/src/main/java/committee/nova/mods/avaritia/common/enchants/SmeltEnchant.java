//package committee.nova.mods.avaritia.common.enchants;
//
//import committee.nova.mods.avaritia.util.ToolUtils;
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.util.Mth;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.SimpleContainer;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.item.ItemEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.AbstractCookingRecipe;
//import net.minecraft.world.item.crafting.Recipe;
//import net.minecraft.world.item.crafting.RecipeType;
//import net.minecraft.world.item.crafting.SmeltingRecipe;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.minecraft.world.item.enchantment.EnchantmentCategory;
//import net.minecraft.world.item.enchantment.EnchantmentHelper;
//import net.minecraft.world.item.enchantment.Enchantments;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.CropBlock;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.event.level.BlockEvent;
//import net.minecraftforge.items.ItemHandlerHelper;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//import java.util.Random;
//
/// **
// * @Project: Avaritia
// * @Author: cnlimiter
// * @CreateTime: 2024/10/20 23:38
// * @Description:
// */
//public class SmeltEnchant extends Enchantment {
//    public SmeltEnchant() {
//        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER,  new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
//    }
//
//    @Override
//    public int getMinCost(int pEnchantmentLevel) {
//        return pEnchantmentLevel * 10;
//    }
//
//    @Override
//    public int getMaxCost(int pEnchantmentLevel) {
//        return this.getMinCost(pEnchantmentLevel) + 15;
//    }
//
//    @Override
//    public int getMaxLevel() {
//        return 4;
//    }
//
//    @Override
//    public boolean checkCompatibility(@NotNull Enchantment pEnch) {
//        return super.checkCompatibility(pEnch) && pEnch != Enchantments.SILK_TOUCH;
//    }
//
//
//}
