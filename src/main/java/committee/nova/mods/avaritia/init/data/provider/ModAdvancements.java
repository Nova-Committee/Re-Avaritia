package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.init.registry.ModTags;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * ModAdvancements
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/1 20:19
 */
public class ModAdvancements extends ForgeAdvancementProvider {

    public ModAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new AvaritiaAdvancements()));
    }

    private interface ModAdvancementSubProvider {
        void generate(@NotNull Advancement root, @NotNull HolderLookup.Provider holderProvider, @NotNull Consumer<Advancement> consumer);
    }

    private static class AvaritiaAdvancements implements AdvancementGenerator {

        private final List<ModAdvancementSubProvider> subProvider = List.of(new MainAdvancements());

        @Override
        public void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<Advancement> consumer, @NotNull ExistingFileHelper existingFileHelper) {

            Advancement root = Advancement.Builder.advancement()
                    .display(ModItems.infinity_catalyst.get(), Component.translatable("advancements.avaritia.start.title"),
                    Component.translatable("advancements.avaritia.start.desc"),
                            new ResourceLocation(Const.MOD_ID, "textures/block/resource/neutron.png"), FrameType.TASK, false, false, false)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_catalyst.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/root");

            this.subProvider.forEach(provider -> provider.generate(root, registries, consumer));
        }
    }

    private static class MainAdvancements implements ModAdvancementSubProvider {
        private final List<ModAdvancementSubProvider> subProvider = List.of(new SingularityAdvancements());

        @Override
        public void generate(@NotNull Advancement root, HolderLookup.@NotNull Provider holderProvider, @NotNull Consumer<Advancement> consumer) {

            Advancement compressed_crafting_table = Advancement.Builder.advancement()
                    .display(ModBlocks.compressed_crafting_table.get(), Component.translatable("advancements.avaritia.compressed_crafting_table.title"), Component.translatable("advancements.avaritia.compressed_crafting_table.desc"), null, FrameType.TASK, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.compressed_crafting_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/compressed_crafting_table");


            Advancement sculk_crafting_table = Advancement.Builder.advancement()
                    .display(ModBlocks.sculk_crafting_table.get(), Component.translatable("advancements.avaritia.sculk_crafting_table.title"), Component.translatable("advancements.avaritia.sculk_crafting_table.desc"), null, FrameType.TASK, true, true, true)
                    .parent(compressed_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.sculk_crafting_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/sculk_crafting_table");


            Advancement diamond_lattice = Advancement.Builder.advancement()
                    .display(ModItems.diamond_lattice.get(), Component.translatable("advancements.avaritia.diamond_lattice.title"), Component.translatable("advancements.avaritia.diamond_lattice.desc"), null, FrameType.TASK, true, true, true)
                    .parent(sculk_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.diamond_lattice.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/diamond_lattice");

            Advancement crystal_matrix_ingot = Advancement.Builder.advancement()
                    .display(ModItems.crystal_matrix_ingot.get(), Component.translatable("advancements.avaritia.crystal_matrix_ingot.title"), Component.translatable("advancements.avaritia.crystal_matrix_ingot.desc"), null, FrameType.TASK, true, true, true)
                    .parent(diamond_lattice)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.crystal_matrix_ingot.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/crystal_matrix_ingot");


            Advancement crystal_pickaxe = Advancement.Builder.advancement()
                    .display(ModItems.crystal_pickaxe.get(), Component.translatable("advancements.avaritia.crystal_pickaxe.title"), Component.translatable("advancements.avaritia.crystal_pickaxe.desc"), null, FrameType.TASK, true, true, true)
                    .parent(crystal_matrix_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.crystal_pickaxe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/crystal_pickaxe");

            Advancement nether_crafting_table = Advancement.Builder.advancement()
                    .display(ModBlocks.nether_crafting_table.get(), Component.translatable("advancements.avaritia.nether_crafting_table.title"), Component.translatable("advancements.avaritia.nether_crafting_table.desc"), null, FrameType.TASK, true, true, true)
                    .parent(sculk_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.nether_crafting_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/nether_crafting_table");

            Advancement blaze_cube = Advancement.Builder.advancement()
                    .display(ModItems.blaze_cube.get(), Component.translatable("advancements.avaritia.blaze_cube.title"), Component.translatable("advancements.avaritia.blaze_cube.desc"), null, FrameType.TASK, true, true, true)
                    .parent(nether_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.blaze_cube.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/blaze_cube");

            Advancement blaze_sword = Advancement.Builder.advancement()
                    .display(ModItems.blaze_sword.get(), Component.translatable("advancements.avaritia.blaze_sword.title"), Component.translatable("advancements.avaritia.blaze_sword.desc"), null, FrameType.TASK, true, true, true)
                    .parent(blaze_cube)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.blaze_sword.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/blaze_sword");

            Advancement blaze_axe = Advancement.Builder.advancement()
                    .display(ModItems.blaze_axe.get(), Component.translatable("advancements.avaritia.blaze_axe.title"), Component.translatable("advancements.avaritia.blaze_axe.desc"), null, FrameType.TASK, true, true, true)
                    .parent(blaze_cube)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.blaze_axe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/blaze_axe");

            Advancement refined_coal = Advancement.Builder.advancement()
                    .display(ModItems.refined_coal.get(), Component.translatable("advancements.avaritia.refined_coal.title"), Component.translatable("advancements.avaritia.refined_coal.desc"), null, FrameType.TASK, true, true, true)
                    .parent(blaze_axe)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.refined_coal.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/refined_coal");

            Advancement star_fuel = Advancement.Builder.advancement()
                    .display(ModItems.star_fuel.get(), Component.translatable("advancements.avaritia.star_fuel.title"), Component.translatable("advancements.avaritia.star_fuel.desc"), null, FrameType.TASK, true, true, true)
                    .parent(refined_coal)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.star_fuel.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/star_fuel");

            Advancement end_crafting_table = Advancement.Builder.advancement()
                    .display(ModBlocks.end_crafting_table.get(), Component.translatable("advancements.avaritia.end_crafting_table.title"), Component.translatable("advancements.avaritia.end_crafting_table.desc"), null, FrameType.TASK, true, true, true)
                    .parent(nether_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.end_crafting_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/end_crafting_table");

            Advancement extreme_crafting_table = Advancement.Builder.advancement()
                    .display(ModBlocks.extreme_crafting_table.get(), Component.translatable("advancements.avaritia.extreme_crafting_table.title"), Component.translatable("advancements.avaritia.extreme_crafting_table.desc"), null, FrameType.TASK, true, true, true)
                    .parent(end_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.extreme_crafting_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/extreme_crafting_table");

            Advancement neutron_collector = Advancement.Builder.advancement()
                    .display(ModBlocks.neutron_collector.get(), Component.translatable("advancements.avaritia.neutron_collector.title"), Component.translatable("advancements.avaritia.neutron_collector.desc"), null, FrameType.TASK, true, true, true)
                    .parent(extreme_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.neutron_collector.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/neutron_collector");

            Advancement neutron_pile = Advancement.Builder.advancement()
                    .display(ModItems.neutron_pile.get(), Component.translatable("advancements.avaritia.neutron_pile.title"), Component.translatable("advancements.avaritia.neutron_pile.desc"), null, FrameType.TASK, true, true, true)
                    .parent(neutron_collector)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.neutron_pile.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/neutron_pile");

            Advancement neutron_ingot = Advancement.Builder.advancement()
                    .display(ModItems.neutron_ingot.get(), Component.translatable("advancements.avaritia.neutron_ingot.title"), Component.translatable("advancements.avaritia.neutron_ingot.desc"), null, FrameType.TASK, true, true, true)
                    .parent(neutron_pile)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.neutron_ingot.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/neutron_ingot");

            Advancement neutron_compressor = Advancement.Builder.advancement()
                    .display(ModBlocks.neutron_compressor.get(), Component.translatable("advancements.avaritia.neutron_compressor.title"), Component.translatable("advancements.avaritia.neutron_compressor.desc"), null, FrameType.TASK, true, true, true)
                    .parent(neutron_pile)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.neutron_compressor.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/neutron_compressor");

            Advancement singularity = Advancement.Builder.advancement()
                    .display(SingularityUtils.getItemForSingularity(ModSingularities.REDSTONE), Component.translatable("advancements.avaritia.singularity.title"), Component.translatable("advancements.avaritia.singularity.desc"), null, FrameType.GOAL, true, true, true)
                    .parent(neutron_compressor)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ModTags.SINGULARITY).build()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":main/singularity");

            this.subProvider.forEach(provider -> provider.generate(singularity, holderProvider, consumer));

        }
    }

    private static class SingularityAdvancements implements ModAdvancementSubProvider {
        private final List<ModAdvancementSubProvider> subProvider = List.of(new InfinityAdvancements());

        @Override
        public void generate(@NotNull Advancement root, HolderLookup.@NotNull Provider holderProvider, @NotNull Consumer<Advancement> consumer) {

            Advancement infinity_catalyst = Advancement.Builder.advancement()
                    .display(ModItems.infinity_catalyst.get(), Component.translatable("advancements.avaritia.infinity_catalyst.title"), Component.translatable("advancements.avaritia.infinity_catalyst.desc"), null, FrameType.TASK, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_catalyst.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":singularity/infinity_catalyst");

            Advancement eternal_singularity = Advancement.Builder.advancement()
                    .display(ModItems.eternal_singularity.get(), Component.translatable("advancements.avaritia.eternal_singularity.title"), Component.translatable("advancements.avaritia.eternal_singularity.desc"), null, FrameType.TASK, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.eternal_singularity.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":singularity/eternal_singularity");
            this.subProvider.forEach(provider -> provider.generate(infinity_catalyst, holderProvider, consumer));

        }
    }

    private static class InfinityAdvancements implements ModAdvancementSubProvider {

        @Override
        public void generate(@NotNull Advancement root, HolderLookup.@NotNull Provider holderProvider, @NotNull Consumer<Advancement> consumer) {
            Advancement infinity_ingot = Advancement.Builder.advancement()
                    .display(ModItems.infinity_ingot.get(), Component.translatable("advancements.avaritia.infinity_ingot.title"), Component.translatable("advancements.avaritia.infinity_ingot.desc"), null, FrameType.TASK.TASK, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_ingot.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_ingot");


            Advancement infinity_umbrella = Advancement.Builder.advancement()
                    .display(ModItems.infinity_umbrella.get(), Component.translatable("advancements.avaritia.infinity_umbrella.title"), Component.translatable("advancements.avaritia.infinity_umbrella.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_umbrella.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_umbrella");

            Advancement infinity_clock = Advancement.Builder.advancement()
                    .display(ModItems.infinity_clock.get(), Component.translatable("advancements.avaritia.infinity_clock.title"), Component.translatable("advancements.avaritia.infinity_clock.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_clock.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_clock");

            Advancement upgrade_smithing_template = Advancement.Builder.advancement()
                    .display(ModItems.upgrade_smithing_template.get(), Component.translatable("advancements.avaritia.upgrade_smithing_template.title"), Component.translatable("advancements.avaritia.upgrade_smithing_template.desc"), null, FrameType.TASK.GOAL, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.upgrade_smithing_template.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/upgrade_smithing_template");

            Advancement enhancement_core = Advancement.Builder.advancement()
                    .display(ModItems.enhancement_core.get(), Component.translatable("advancements.avaritia.enhancement_core.title"), Component.translatable("advancements.avaritia.enhancement_core.desc"), null, FrameType.TASK.GOAL, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.enhancement_core.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/enhancement_core");

            Advancement extreme_smithing_table = Advancement.Builder.advancement()
                    .display(ModBlocks.extreme_smithing_table.get(), Component.translatable("advancements.avaritia.extreme_smithing_table.title"), Component.translatable("advancements.avaritia.extreme_smithing_table.desc"), null, FrameType.TASK.GOAL, true, true, true)
                    .parent(enhancement_core)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.extreme_smithing_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/extreme_smithing_table");



            Advancement infinity_pickaxe = Advancement.Builder.advancement()
                    .display(ModItems.infinity_pickaxe.get(), Component.translatable("advancements.avaritia.infinity_pickaxe.title"), Component.translatable("advancements.avaritia.infinity_pickaxe.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_pickaxe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_pickaxe");

            Advancement matter_cluster = Advancement.Builder.advancement()
                    .display(ModItems.matter_cluster.get(), Component.translatable("advancements.avaritia.matter_cluster.title"), Component.translatable("advancements.avaritia.matter_cluster.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_pickaxe)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.matter_cluster.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/matter_cluster");

            Advancement infinity_shovel = Advancement.Builder.advancement()
                    .display(ModItems.infinity_shovel.get(), Component.translatable("advancements.avaritia.infinity_shovel.title"), Component.translatable("advancements.avaritia.infinity_shovel.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_shovel.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_shovel");

            Advancement infinity_hoe = Advancement.Builder.advancement()
                    .display(ModItems.infinity_hoe.get(), Component.translatable("advancements.avaritia.infinity_hoe.title"), Component.translatable("advancements.avaritia.infinity_hoe.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_hoe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_hoe");

            Advancement infinity_axe = Advancement.Builder.advancement()
                    .display(ModItems.infinity_axe.get(), Component.translatable("advancements.avaritia.infinity_axe.title"), Component.translatable("advancements.avaritia.infinity_axe.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_axe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_axe");

            Advancement infinity_sword = Advancement.Builder.advancement()
                    .display(ModItems.infinity_sword.get(), Component.translatable("advancements.avaritia.infinity_sword.title"), Component.translatable("advancements.avaritia.infinity_sword.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_sword.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_sword");

            Advancement infinity_bow = Advancement.Builder.advancement()
                    .display(ModItems.infinity_bow.get(), Component.translatable("advancements.avaritia.infinity_bow.title"), Component.translatable("advancements.avaritia.infinity_bow.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_bow.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_bow");

            Advancement infinity_armor = Advancement.Builder.advancement()
                    .display(ModItems.infinity_chestplate.get(), Component.translatable("advancements.avaritia.infinity_armor.title"), Component.translatable("advancements.avaritia.infinity_armor.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.infinity_helmet.get(),
                            ModItems.infinity_chestplate.get(),
                            ModItems.infinity_pants.get(),
                            ModItems.infinity_boots.get()
                    ))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_armor");

            Advancement infinity_food = Advancement.Builder.advancement()
                    .display(ModItems.ultimate_stew.get(), Component.translatable("advancements.avaritia.infinity_food.title"), Component.translatable("advancements.avaritia.infinity_food.desc"), null, FrameType.TASK.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("food0", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.ultimate_stew.get()))
                    .addCriterion("food1", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.cosmic_meatballs.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Const.MOD_ID + ":infinity/infinity_food");
        }
    }
}
