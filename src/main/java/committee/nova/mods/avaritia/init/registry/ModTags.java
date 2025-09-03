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
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/1/8 22:43
 * @Description:
 */

public class ModTags {
    public static final TagKey<Item> SINGULARITY = ItemTags.create(Const.rl("singularity"));
    //2025.08.23 cu6
    public static final TagKey<Item> IMMORTAL_ITEM = ItemTags.create(Const.rl("endless"));
    public static final TagKey<Item> DRAWERS = ItemTags.create(new ResourceLocation("storagedrawers", "drawers"));

    public static final TagKey<Item> NEUTRON_DUST = ItemTags.create(new ResourceLocation("forge", "dust/neutronium"));
    public static final TagKey<Item> NEUTRON_NUGGET = ItemTags.create(new ResourceLocation("forge", "nuggets/neutronium"));
    public static final TagKey<Item> NEUTRON_INGOT = ItemTags.create(new ResourceLocation("forge", "ingot/neutronium"));
    public static final TagKey<Block> NEUTRON_BLOCK = BlockTags.create(new ResourceLocation("forge", "storage_blocks/neutronium"));

    public static final TagKey<Block> EXTREME_ANVIL_UNBREAK = BlockTags.create(Const.rl("extreme_anvil_unbreak"));
    public static final TagKey<Block> NEEDS_CRYSTAL_TOOL = BlockTags.create(Const.rl("needs_crystal_tool"));
    public static final TagKey<Block> NEEDS_BLAZE_TOOL = BlockTags.create(Const.rl("needs_blaze_tool"));
    public static final TagKey<Block> NEEDS_INFINITY_TOOL = BlockTags.create(Const.rl("needs_infinity_tool"));

    public static final TagKey<EntityType<?>> NEUTRAL_CREATURES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge", "neutral_creatures"));

}
