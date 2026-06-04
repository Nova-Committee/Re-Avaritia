package com.avaritia.init.registry;

import com.avaritia.Const;

import com.avaritia.Avaritia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 注册模组中的所有标签。
 */
public class ModTags {
    public static final TagKey<Item> SINGULARITY = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Const.MOD_ID, "singularity"));
    public static final TagKey<Item> IMMORTAL_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Const.MOD_ID, "endless"));
    public static final TagKey<Item> ELYTRA_SLOT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("elytraslot", "elytra"));

    public static final TagKey<Item> NEUTRON_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dust/neutronium"));
    public static final TagKey<Item> NEUTRON_GEAR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "gears/neutronium"));
    public static final TagKey<Item> NEUTRON_NUGGET = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/neutronium"));
    public static final TagKey<Item> NEUTRON_INGOT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/neutronium"));
    public static final TagKey<Item> NEUTRON_BLOCK_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/neutronium"));
    public static final TagKey<Block> NEUTRON_BLOCK = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks/neutronium"));

    public static final TagKey<Block> EXTREME_ANVIL_UNBREAK = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Const.MOD_ID, "extreme_anvil_unbreak"));
    public static final TagKey<Block> NEEDS_CRYSTAL_TOOL = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Const.MOD_ID, "needs_crystal_tool"));
    public static final TagKey<Block> NEEDS_BLAZE_TOOL = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Const.MOD_ID, "needs_blaze_tool"));
    public static final TagKey<Block> NEEDS_INFINITY_TOOL = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Const.MOD_ID, "needs_infinity_tool"));

    public static final TagKey<EntityType<?>> NEUTRAL_CREATURES = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "neutral_creatures"));
}
