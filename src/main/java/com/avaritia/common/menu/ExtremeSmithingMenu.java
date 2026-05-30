package com.avaritia.common.menu;

import com.avaritia.common.crafting.input.ExtremeSmithingRecipeInput;
import com.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModMenus;
import com.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/19 12:27
 * @Description:
 */
public class ExtremeSmithingMenu extends ItemCombinerMenu {
    private final Level level;
    private final RecipePropertySet baseItemTest;
    private final RecipePropertySet templateItemTest;
    private final RecipePropertySet additionItemTest1;
    private final RecipePropertySet additionItemTest2;
    private final RecipePropertySet additionItemTest3;
    private final DataSlot hasRecipeError = DataSlot.standalone();



    public ExtremeSmithingMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeSmithingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess access) {
        super(ModMenus.extreme_smithing_table.get(), pContainerId, pPlayerInventory, access, createInputSlotDefinitions(pPlayerInventory.player.level().recipeAccess()));
        this.level = pPlayerInventory.player.level();
        this.baseItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_BASE);
        this.templateItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        this.additionItemTest1 = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
        this.additionItemTest2 = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
        this.additionItemTest3 = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions(RecipeAccess recipes) {
        RecipePropertySet baseItemTest = recipes.propertySet(RecipePropertySet.SMITHING_BASE);
        RecipePropertySet templateItemTest = recipes.propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        RecipePropertySet additionItemTest1 = recipes.propertySet(RecipePropertySet.SMITHING_ADDITION);
        RecipePropertySet additionItemTest2 = recipes.propertySet(RecipePropertySet.SMITHING_ADDITION);
        RecipePropertySet additionItemTest3 = recipes.propertySet(RecipePropertySet.SMITHING_ADDITION);
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 31, 35, templateItemTest::test)
                .withSlot(1, 49, 35, baseItemTest::test)
                .withSlot(2, 67, 35, additionItemTest1::test)
                .withSlot(3, 49, 17, additionItemTest2::test)
                .withSlot(4, 49, 53, additionItemTest3::test)
                .withResultSlot(5, 121, 35)
                .build();
    }


    @Override
    protected boolean isValidBlock(@NonNull BlockState pState) {
        return pState.is(ModBlocks.extreme_smithing_table.get());
    }

    @Override
    protected void onTake(@NonNull Player player, ItemStack carried) {
        carried.onCraftedBy(player, carried.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.shrinkStackInSlot(3);
        this.shrinkStackInSlot(4);
        this.access.execute((level, pos) -> level.levelEvent(1044, pos, 0));
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private ExtremeSmithingRecipeInput createRecipeInput() {
        return new ExtremeSmithingRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1)
                , this.inputSlots.getItem(2), this.inputSlots.getItem(3), this.inputSlots.getItem(4));
    }

    private void shrinkStackInSlot(int slot) {
        ItemStack stack = this.inputSlots.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            this.inputSlots.setItem(slot, stack);
        }
    }

    @Override
    public void slotsChanged(@NonNull Container container) {
        super.slotsChanged(container);
        if (this.level instanceof ServerLevel) {
            boolean hasRecipeError = this.getSlot(0).hasItem()
                    && this.getSlot(1).hasItem()
                    && this.getSlot(2).hasItem()
                    && this.getSlot(3).hasItem()
                    && this.getSlot(4).hasItem()
                    && !this.getSlot(this.getResultSlot()).hasItem();
            this.hasRecipeError.set(hasRecipeError ? 1 : 0);
        }
    }

    @Override
    public void createResult() {
        ExtremeSmithingRecipeInput input = this.createRecipeInput();
        Optional<RecipeHolder<ExtremeSmithingRecipe>> foundRecipe;
        if (this.level instanceof ServerLevel serverLevel) {
            foundRecipe = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get(), input, serverLevel);
        } else {
            foundRecipe = Optional.empty();
        }

        foundRecipe.ifPresentOrElse(recipe -> {
            ItemStack result = recipe.value().assemble(input);
            this.resultSlots.setRecipeUsed((RecipeHolder<?>)recipe);
            this.resultSlots.setItem(0, result);
        }, () -> {
            this.resultSlots.setRecipeUsed(null);
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != this.resultSlots && super.canTakeItemForPickAll(carried, target);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack stack) {
        if (this.templateItemTest.test(stack) && !this.getSlot(0).hasItem()) {
            return true;
        } else {
            return this.baseItemTest.test(stack)
                    && !this.getSlot(1).hasItem() || (this.additionItemTest1.test(stack)
                    && !this.getSlot(2).hasItem() || (this.additionItemTest2.test(stack)
                    && !this.getSlot(3).hasItem() || this.additionItemTest3.test(stack)
                    && !this.getSlot(4).hasItem()))
                    ;
        }
    }

    public boolean hasRecipeError() {
        return this.hasRecipeError.get() > 0;
    }
}
