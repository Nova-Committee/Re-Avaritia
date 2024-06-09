package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Static;
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
                    .display(ModItems.diamond_lattice.get(), Component.translatable("advancements.avaritia.diamond_lattice.title"),
                            Component.translatable("advancements.avaritia.diamond_lattice.desc"),
                            new ResourceLocation(Static.MOD_ID, "textures/blocks/resource/neutronium.png"), FrameType.TASK, false, false, false)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.diamond_lattice.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/root");

            this.subProvider.forEach(provider -> provider.generate(root, registries, consumer));
        }
    }

    private static class MainAdvancements implements ModAdvancementSubProvider {
        private final List<ModAdvancementSubProvider> subProvider = List.of(new SingularityAdvancements());

        @Override
        public void generate(@NotNull Advancement root, HolderLookup.@NotNull Provider holderProvider, @NotNull Consumer<Advancement> consumer) {
            Advancement neutron_pile = Advancement.Builder.advancement()
                    .display(ModItems.neutron_pile.get(), Component.translatable("advancements.avaritia.neutron_pile.title"), Component.translatable("advancements.avaritia.neutron_pile.desc"), null, FrameType.TASK, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.neutron_pile.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/neutron_pile");


            Advancement extreme_crafting_table = Advancement.Builder.advancement()
                    .display(ModBlocks.extreme_crafting_table.get(), Component.translatable("advancements.avaritia.extreme_crafting_table.title"), Component.translatable("advancements.avaritia.extreme_crafting_table.desc"), null, FrameType.TASK, true, true, true)
                    .parent(neutron_pile)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.extreme_crafting_table.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/extreme_crafting_table");
            Advancement skull_fire_sword = Advancement.Builder.advancement()
                    .display(ModItems.skull_sword.get(), Component.translatable("advancements.avaritia.skull_sword.title"), Component.translatable("advancements.avaritia.skull_sword.desc"), null, FrameType.TASK, true, true, true)
                    .parent(extreme_crafting_table)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.skull_sword.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/skull_sword");

            Advancement neutron_ingot = Advancement.Builder.advancement()
                    .display(ModItems.neutron_ingot.get(), Component.translatable("advancements.avaritia.neutron_ingot.title"), Component.translatable("advancements.avaritia.neutron_ingot.desc"), null, FrameType.GOAL, true, true, true)
                    .parent(neutron_pile)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.neutron_ingot.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/neutron_ingot");

            Advancement endest_pearl = Advancement.Builder.advancement()
                    .display(ModItems.endest_pearl.get(), Component.translatable("advancements.avaritia.endest_pearl.title"), Component.translatable("advancements.avaritia.endest_pearl.desc"), null, FrameType.GOAL, true, true, true)
                    .parent(neutron_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.endest_pearl.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/endest_pearl");

            Advancement neutron_compressor = Advancement.Builder.advancement()
                    .display(ModBlocks.neutron_compressor.get(), Component.translatable("advancements.avaritia.neutron_compressor.title"), Component.translatable("advancements.avaritia.neutron_compressor.desc"), null, FrameType.TASK, true, true, true)
                    .parent(neutron_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.neutron_compressor.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/neutron_compressor");
            Advancement singularity = Advancement.Builder.advancement()
                    .display(SingularityUtils.getItemForSingularity(ModSingularities.REDSTONE), Component.translatable("advancements.avaritia.singularity.title"), Component.translatable("advancements.avaritia.singularity.desc"), null, FrameType.GOAL, true, true, true)
                    .parent(neutron_compressor)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ModTags.SINGULARITY).build()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":main/singularity");

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
                    .save(consumer, Static.MOD_ID + ":singularity/infinity_catalyst");
            this.subProvider.forEach(provider -> provider.generate(infinity_catalyst, holderProvider, consumer));
        }
    }

    private static class InfinityAdvancements implements ModAdvancementSubProvider {

        @Override
        public void generate(@NotNull Advancement root, HolderLookup.@NotNull Provider holderProvider, @NotNull Consumer<Advancement> consumer) {
            Advancement infinity_ingot = Advancement.Builder.advancement()
                    .display(ModItems.infinity_ingot.get(), Component.translatable("advancements.avaritia.infinity_ingot.title"), Component.translatable("advancements.avaritia.infinity_ingot.desc"), null, FrameType.TASK, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_ingot.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_ingot");


            Advancement star_fuel = Advancement.Builder.advancement()
                    .display(ModItems.star_fuel.get(), Component.translatable("advancements.avaritia.star_fuel.title"), Component.translatable("advancements.avaritia.star_fuel.desc"), null, FrameType.GOAL, true, true, true)
                    .parent(root)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.star_fuel.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/star_fuel");

            Advancement infinity_pickaxe = Advancement.Builder.advancement()
                    .display(ModItems.infinity_pickaxe.get(), Component.translatable("advancements.avaritia.infinity_pickaxe.title"), Component.translatable("advancements.avaritia.infinity_pickaxe.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_pickaxe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_pickaxe");

            Advancement matter_cluster = Advancement.Builder.advancement()
                    .display(ModItems.matter_cluster.get(), Component.translatable("advancements.avaritia.matter_cluster.title"), Component.translatable("advancements.avaritia.matter_cluster.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_pickaxe)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.matter_cluster.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/matter_cluster");

            Advancement infinity_shovel = Advancement.Builder.advancement()
                    .display(ModItems.infinity_shovel.get(), Component.translatable("advancements.avaritia.infinity_shovel.title"), Component.translatable("advancements.avaritia.infinity_shovel.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_shovel.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_shovel");

            Advancement infinity_hoe = Advancement.Builder.advancement()
                    .display(ModItems.infinity_hoe.get(), Component.translatable("advancements.avaritia.infinity_hoe.title"), Component.translatable("advancements.avaritia.infinity_hoe.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_hoe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_hoe");

            Advancement infinity_axe = Advancement.Builder.advancement()
                    .display(ModItems.infinity_axe.get(), Component.translatable("advancements.avaritia.infinity_axe.title"), Component.translatable("advancements.avaritia.infinity_axe.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_axe.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_axe");

            Advancement infinity_sword = Advancement.Builder.advancement()
                    .display(ModItems.infinity_sword.get(), Component.translatable("advancements.avaritia.infinity_sword.title"), Component.translatable("advancements.avaritia.infinity_sword.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_sword.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_sword");

            Advancement infinity_bow = Advancement.Builder.advancement()
                    .display(ModItems.infinity_bow.get(), Component.translatable("advancements.avaritia.infinity_bow.title"), Component.translatable("advancements.avaritia.infinity_bow.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.infinity_bow.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_bow");

            Advancement infinity_armor = Advancement.Builder.advancement()
                    .display(ModItems.infinity_chestplate.get(), Component.translatable("advancements.avaritia.infinity_armor.title"), Component.translatable("advancements.avaritia.infinity_armor.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.infinity_helmet.get(),
                            ModItems.infinity_chestplate.get(),
                            ModItems.infinity_pants.get(),
                            ModItems.infinity_boots.get()
                    ))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_armor");

            Advancement infinity_food = Advancement.Builder.advancement()
                    .display(ModItems.ultimate_stew.get(), Component.translatable("advancements.avaritia.infinity_food.title"), Component.translatable("advancements.avaritia.infinity_food.desc"), null, FrameType.CHALLENGE, true, true, true)
                    .parent(infinity_ingot)
                    .addCriterion("food0", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.ultimate_stew.get()))
                    .addCriterion("food1", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.cosmic_meatballs.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Static.MOD_ID + ":infinity/infinity_food");
        }
    }
}
