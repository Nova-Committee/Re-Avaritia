package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.crafting.input.ExtremeSmithingRecipeInput;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
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
    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int RIGHT_ADDITION_SLOT = 2;
    public static final int TOP_ADDITION_SLOT = 3;
    public static final int BOTTOM_ADDITION_SLOT = 4;
    public static final int INPUT_SLOT_COUNT = 5;
    public static final int RESULT_SLOT = 5;
    public static final int PLAYER_INVENTORY_SLOT_START = 6;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = 36;

    private final Level level;
    private final List<ExtremeSmithingRecipe> recipes;
    private final DataSlot hasRecipeError = DataSlot.standalone();



    public ExtremeSmithingMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeSmithingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess access) {
        this(pContainerId, pPlayerInventory, access, findRecipes(pPlayerInventory.player.level()));
    }

    private ExtremeSmithingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access,
                                List<ExtremeSmithingRecipe> recipes) {
        super(ModMenus.extreme_smithing_table.get(), containerId, playerInventory, access, createInputSlotDefinitions(recipes));
        this.level = playerInventory.player.level();
        this.recipes = recipes;
    }

    private static List<ExtremeSmithingRecipe> findRecipes(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().recipeMap().byType(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
        }

        // 客户端不会把自定义配方暴露到 RecipeAccess；保持槽位开放，由服务端菜单执行同一配方校验。
        return List.of();
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions(List<ExtremeSmithingRecipe> recipes) {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(TEMPLATE_SLOT, 31, 35, stack -> acceptsTemplate(recipes, stack))
                .withSlot(BASE_SLOT, 49, 35, stack -> acceptsBase(recipes, stack))
                .withSlot(RIGHT_ADDITION_SLOT, 67, 35, stack -> acceptsAddition(recipes, stack))
                .withSlot(TOP_ADDITION_SLOT, 49, 17, stack -> acceptsAddition(recipes, stack))
                .withSlot(BOTTOM_ADDITION_SLOT, 49, 53, stack -> acceptsAddition(recipes, stack))
                .withResultSlot(RESULT_SLOT, 121, 35)
                .build();
    }

    static boolean acceptsTemplate(List<ExtremeSmithingRecipe> recipes, ItemStack stack) {
        return recipes.isEmpty() || recipes.stream().anyMatch(recipe -> recipe.isTemplateIngredient(stack));
    }

    static boolean acceptsBase(List<ExtremeSmithingRecipe> recipes, ItemStack stack) {
        return recipes.isEmpty() || recipes.stream().anyMatch(recipe -> recipe.isBaseIngredient(stack));
    }

    static boolean acceptsAddition(List<ExtremeSmithingRecipe> recipes, ItemStack stack) {
        return recipes.isEmpty() || recipes.stream().anyMatch(recipe -> recipe.isAdditionIngredient(stack));
    }


    @Override
    protected boolean isValidBlock(@NonNull BlockState pState) {
        return pState.is(ModBlocks.extreme_smithing_table.get());
    }

    @Override
    protected void onTake(@NonNull Player player, ItemStack carried) {
        carried.onCraftedBy(player, carried.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            this.shrinkStackInSlot(slot);
        }
        this.access.execute((level, pos) -> level.levelEvent(1044, pos, 0));
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(
                this.inputSlots.getItem(TEMPLATE_SLOT),
                this.inputSlots.getItem(BASE_SLOT),
                this.inputSlots.getItem(RIGHT_ADDITION_SLOT),
                this.inputSlots.getItem(TOP_ADDITION_SLOT),
                this.inputSlots.getItem(BOTTOM_ADDITION_SLOT)
        );
    }

    private ExtremeSmithingRecipeInput createRecipeInput() {
        return new ExtremeSmithingRecipeInput(
                this.inputSlots.getItem(TEMPLATE_SLOT),
                this.inputSlots.getItem(BASE_SLOT),
                this.inputSlots.getItem(RIGHT_ADDITION_SLOT),
                this.inputSlots.getItem(TOP_ADDITION_SLOT),
                this.inputSlots.getItem(BOTTOM_ADDITION_SLOT)
        );
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
            boolean hasRecipeError = this.getSlot(TEMPLATE_SLOT).hasItem()
                    && this.getSlot(BASE_SLOT).hasItem()
                    && this.getSlot(RIGHT_ADDITION_SLOT).hasItem()
                    && this.getSlot(TOP_ADDITION_SLOT).hasItem()
                    && this.getSlot(BOTTOM_ADDITION_SLOT).hasItem()
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
        return acceptsTemplate(this.recipes, stack)
                || acceptsBase(this.recipes, stack)
                || acceptsAddition(this.recipes, stack);
    }

    public boolean hasRecipeError() {
        return this.hasRecipeError.get() > 0;
    }
}
