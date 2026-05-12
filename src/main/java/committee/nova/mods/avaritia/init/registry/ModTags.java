package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */

public class ModTags {
    public static final TagKey<Item> SINGULARITY = ItemTags.create(Const.rl("singularity"));
    public static final TagKey<Item> IMMORTAL_ITEM = ItemTags.create(Const.rl("endless"));
    public static final TagKey<Item> DRAWERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("storagedrawers", "drawers"));

    public static final TagKey<Item> ELYTRA_SLOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("elytraslot", "elytra"));

    public static final TagKey<Item> NEUTRON_DUST = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dust/neutronium"));
    public static final TagKey<Item> NEUTRON_GEAR = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gears/neutronium"));
    public static final TagKey<Item> NEUTRON_NUGGET = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/neutronium"));
    public static final TagKey<Item> NEUTRON_INGOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/neutronium"));
    public static final TagKey<Item> NEUTRON_BLOCK_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/neutronium"));
    public static final TagKey<Block> NEUTRON_BLOCK = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/neutronium"));

    public static final TagKey<Block> EXTREME_ANVIL_UNBREAK = BlockTags.create(Const.rl("extreme_anvil_unbreak"));
    public static final TagKey<Block> NEEDS_CRYSTAL_TOOL = BlockTags.create(Const.rl("needs_crystal_tool"));
    public static final TagKey<Block> NEEDS_BLAZE_TOOL = BlockTags.create(Const.rl("needs_blaze_tool"));
    public static final TagKey<Block> NEEDS_INFINITY_TOOL = BlockTags.create(Const.rl("needs_infinity_tool"));

    public static final TagKey<EntityType<?>> NEUTRAL_CREATURES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c","neutral_creatures"));
}
