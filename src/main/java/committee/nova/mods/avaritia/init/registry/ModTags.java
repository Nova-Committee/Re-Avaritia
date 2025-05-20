package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/1/8 22:43
 * @Description:
 */

public class ModTags {
    public static final TagKey<Item> SINGULARITY = ItemTags.create(Static.rl("singularity"));
    public static final TagKey<Item> DRAWERS = ItemTags.create(ResourceLocation.tryBuild("storagedrawers", "drawers"));

    public static final TagKey<Item> NEUTRON_DUST = ItemTags.create(ResourceLocation.tryBuild("c", "dust/neutronium"));
    public static final TagKey<Item> NEUTRON_NUGGET = ItemTags.create(ResourceLocation.tryBuild("c", "nuggets/neutronium"));
    public static final TagKey<Item> NEUTRON_INGOT = ItemTags.create(ResourceLocation.tryBuild("c", "ingot/neutronium"));
    public static final TagKey<Block> NEUTRON_BLOCK = BlockTags.create(ResourceLocation.tryBuild("c", "storage_blocks/neutronium"));

    public static final TagKey<Block> EXTREME_ANVIL_UNBREAK = BlockTags.create(Static.rl("extreme_anvil_unbreak"));
    public static final TagKey<Block> NEEDS_CRYSTAL_TOOL = BlockTags.create(Static.rl("needs_crystal_tool"));
    public static final TagKey<Block> NEEDS_BLAZE_TOOL = BlockTags.create(Static.rl("needs_blaze_tool"));
    public static final TagKey<Block> NEEDS_INFINITY_TOOL = BlockTags.create(Static.rl("needs_infinity_tool"));

    public static final TagKey<EntityType<?>> NEUTRAL_CREATURES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.tryBuild("c","neutral_creatures"));
}
