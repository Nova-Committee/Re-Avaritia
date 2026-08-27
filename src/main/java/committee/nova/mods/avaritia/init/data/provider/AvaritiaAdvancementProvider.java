package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AvaritiaAdvancementProvider implements AdvancementSubProvider {
    public static AdvancementProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new AdvancementProvider(output, registries, List.of(new AvaritiaAdvancementProvider()));
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
        HolderGetter<Item> itemRegistry = registries.lookupOrThrow(Registries.ITEM);
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.infinity_catalyst.get(),
                        Component.translatable("advancements.avaritia.start.title"),
                        Component.translatable("advancements.avaritia.start.desc"),
                        Const.rl("gui/advancements/backgrounds/neutron"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.compressed_crafting_table.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(writer, Const.MOD_ID + ":main/root");

        AdvancementHolder compressed = item(root, writer, "compressed_crafting_table", ModBlocks.compressed_crafting_table.get(), AdvancementType.TASK, "main");
        AdvancementHolder sculk = item(compressed, writer, "sculk_crafting_table", ModBlocks.sculk_crafting_table.get(), AdvancementType.TASK, "main");
        AdvancementHolder diamondLattice = item(sculk, writer, "diamond_lattice", ModItems.diamond_lattice.get(), AdvancementType.TASK, "main");
        AdvancementHolder crystalIngot = item(diamondLattice, writer, "crystal_matrix_ingot", ModItems.crystal_matrix_ingot.get(), AdvancementType.TASK, "main");
        item(crystalIngot, writer, "crystal_pickaxe", ModItems.crystal_pickaxe.get(), AdvancementType.TASK, "main");

        AdvancementHolder nether = item(sculk, writer, "nether_crafting_table", ModBlocks.nether_crafting_table.get(), AdvancementType.TASK, "main");
        AdvancementHolder blazeCube = item(nether, writer, "blaze_cube", ModItems.blaze_cube.get(), AdvancementType.TASK, "main");
        item(blazeCube, writer, "blaze_sword", ModItems.blaze_sword.get(), AdvancementType.TASK, "main");
        AdvancementHolder blazeAxe = item(blazeCube, writer, "blaze_axe", ModItems.blaze_axe.get(), AdvancementType.TASK, "main");
        AdvancementHolder refinedCoal = item(blazeAxe, writer, "refined_coal", ModItems.refined_coal.get(), AdvancementType.TASK, "main");
        item(refinedCoal, writer, "star_fuel", ModItems.star_fuel.get(), AdvancementType.TASK, "main");

        AdvancementHolder end = item(nether, writer, "end_crafting_table", ModBlocks.end_crafting_table.get(), AdvancementType.TASK, "main");
        AdvancementHolder extreme = item(end, writer, "extreme_crafting_table", ModBlocks.extreme_crafting_table.get(), AdvancementType.TASK, "main");
        AdvancementHolder collector = item(extreme, writer, "neutron_collector", ModBlocks.neutron_collector.get(), AdvancementType.TASK, "main");
        AdvancementHolder pile = item(collector, writer, "neutron_pile", ModItems.neutron_pile.get(), AdvancementType.TASK, "main");
        AdvancementHolder neutronIngot = item(pile, writer, "neutron_ingot", ModItems.neutron_ingot.get(), AdvancementType.TASK, "main");
        AdvancementHolder compressor = item(neutronIngot, writer, "neutron_compressor", ModBlocks.neutron_compressor.get(), AdvancementType.TASK, "main");
        AdvancementHolder singularity = singularity(compressor, writer, itemRegistry);

        AdvancementHolder infinityCatalyst = item(singularity, writer, "infinity_catalyst", ModItems.infinity_catalyst.get(), AdvancementType.TASK, "singularity");
        item(singularity, writer, "eternal_singularity", ModItems.eternal_singularity.get(), AdvancementType.TASK, "singularity");
        AdvancementHolder infinityIngot = item(infinityCatalyst, writer, "infinity_ingot", ModItems.infinity_ingot.get(), AdvancementType.TASK, "infinity");

        item(infinityIngot, writer, "infinity_umbrella", ModItems.infinity_umbrella.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "infinity_clock", ModItems.infinity_clock.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "upgrade_smithing_template", ModItems.upgrade_smithing_template.get(), AdvancementType.GOAL, "infinity");
        AdvancementHolder enhancement = item(infinityIngot, writer, "enhancement_core", ModItems.enhancement_core.get(), AdvancementType.GOAL, "infinity");
        item(enhancement, writer, "extreme_smithing_table", ModBlocks.extreme_smithing_table.get(), AdvancementType.GOAL, "infinity");
        AdvancementHolder pickaxe = item(infinityIngot, writer, "infinity_pickaxe", ModItems.infinity_pickaxe.get(), AdvancementType.CHALLENGE, "infinity");
        item(pickaxe, writer, "matter_cluster", ModItems.matter_cluster.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "infinity_shovel", ModItems.infinity_shovel.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "infinity_hoe", ModItems.infinity_hoe.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "infinity_axe", ModItems.infinity_axe.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "infinity_sword", ModItems.infinity_sword.get(), AdvancementType.CHALLENGE, "infinity");
        item(infinityIngot, writer, "infinity_bow", ModItems.infinity_bow.get(), AdvancementType.CHALLENGE, "infinity");
        armor(infinityIngot, writer);
        food(infinityIngot, writer, itemRegistry);
    }

    private static AdvancementHolder item(AdvancementHolder parent, Consumer<AdvancementHolder> writer, String key, ItemLike icon, AdvancementType type, String folder) {
        return Advancement.Builder.advancement()
                .display(icon,
                        Component.translatable("advancements.avaritia." + key + ".title"),
                        Component.translatable("advancements.avaritia." + key + ".desc"),
                        null,
                        type,
                        true,
                        true,
                        true)
                .parent(parent)
                .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(icon))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(writer, Const.MOD_ID + ":" + folder + "/" + key);
    }

    private static AdvancementHolder singularity(AdvancementHolder parent, Consumer<AdvancementHolder> writer, HolderGetter<Item> itemRegistry) {
        return Advancement.Builder.advancement()
                .display(ModItems.singularity.get(),
                        Component.translatable("advancements.avaritia.singularity.title"),
                        Component.translatable("advancements.avaritia.singularity.desc"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        true)
                .parent(parent)
                .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemRegistry, ModTags.SINGULARITY)))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(writer, Const.MOD_ID + ":main/singularity");
    }

    private static AdvancementHolder armor(AdvancementHolder parent, Consumer<AdvancementHolder> writer) {
        return Advancement.Builder.advancement()
                .display(ModItems.infinity_chestplate.get(),
                        Component.translatable("advancements.avaritia.infinity_armor.title"),
                        Component.translatable("advancements.avaritia.infinity_armor.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true)
                .parent(parent)
                .addCriterion("main", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ModItems.infinity_helmet.get(),
                        ModItems.infinity_chestplate.get(),
                        ModItems.infinity_pants.get(),
                        ModItems.infinity_boots.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(writer, Const.MOD_ID + ":infinity/infinity_armor");
    }

    private static AdvancementHolder food(AdvancementHolder parent, Consumer<AdvancementHolder> writer, HolderGetter<Item> itemRegistry) {
        return Advancement.Builder.advancement()
                .display(ModItems.ultimate_stew.get(),
                        Component.translatable("advancements.avaritia.infinity_food.title"),
                        Component.translatable("advancements.avaritia.infinity_food.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true)
                .parent(parent)
                .addCriterion("food0", ConsumeItemTrigger.TriggerInstance.usedItem(itemRegistry, ModItems.ultimate_stew.get()))
                .addCriterion("food1", ConsumeItemTrigger.TriggerInstance.usedItem(itemRegistry, ModItems.cosmic_meatballs.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(writer, Const.MOD_ID + ":infinity/infinity_food");
    }
}
