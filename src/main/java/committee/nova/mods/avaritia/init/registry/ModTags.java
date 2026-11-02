package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @Project: Avaritia-forge
 * @author cnlimiter
 * @CreateTime: 2024/1/8 22:43
 * @Description:
 */

public class ModTags {
    public static final TagKey<Item> SINGULARITY = TagKey.create(Registries.ITEM, Const.rl("singularity"));
    //2025.08.23 cu6
    public static final TagKey<Item> IMMORTAL_ITEM = TagKey.create(Registries.ITEM, Const.rl("endless"));
    public static final TagKey<Item> DRAWERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("storagedrawers", "drawers"));

    public static final TagKey<Item> NEUTRON_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "dust/neutronium"));
    public static final TagKey<Item> NEUTRON_NUGGET = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "nuggets/neutronium"));
    public static final TagKey<Item> NEUTRON_INGOT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "ingots/neutronium"));
    public static final TagKey<Item> INFINITY_ELYTRA = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "elytra"));
    public static final TagKey<Block> NEUTRON_BLOCK = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("forge", "storage_blocks/neutronium"));


    public static final TagKey<Block> EXTREME_ANVIL_UNBREAK = TagKey.create(Registries.BLOCK, Const.rl("extreme_anvil_unbreak"));
    public static final TagKey<Block> NEEDS_CRYSTAL_TOOL = TagKey.create(Registries.BLOCK, Const.rl("needs_crystal_tool"));
    public static final TagKey<Block> NEEDS_BLAZE_TOOL = TagKey.create(Registries.BLOCK, Const.rl("needs_blaze_tool"));
    public static final TagKey<Block> NEEDS_INFINITY_TOOL = TagKey.create(Registries.BLOCK, Const.rl("needs_infinity_tool"));

    public static final TagKey<Block> INCORRECT_FOR_BLAZE_TOOL = TagKey.create(Registries.BLOCK, Const.rl("incorrect_for_blaze_tool"));
    public static final TagKey<Block> INCORRECT_FOR_CRYSTAL_TOOL = TagKey.create(Registries.BLOCK, Const.rl("incorrect_for_crystal_tool"));
    public static final TagKey<Block> INCORRECT_FOR_INFINITY_TOOL = TagKey.create(Registries.BLOCK, Const.rl("incorrect_for_infinity_tool"));
    public static final TagKey<Item> REPAIRS_BLAZE_TOOLS = TagKey.create(Registries.ITEM, Const.rl("repairs_blaze_tools"));
    public static final TagKey<Item> REPAIRS_CRYSTAL_TOOLS = TagKey.create(Registries.ITEM, Const.rl("repairs_crystal_tools"));
    public static final TagKey<Item> REPAIRS_INFINITY_TOOLS = TagKey.create(Registries.ITEM, Const.rl("repairs_infinity_tools"));
    public static final TagKey<Item> REPAIRS_INFINITY_ARMOR = TagKey.create(Registries.ITEM, Const.rl("repairs_infinity_armor"));

    public static final TagKey<EntityType<?>> NEUTRAL_CREATURES = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("forge", "neutral_creatures"));
    public static final TagKey<EntityType<?>> AQUATIC_CAPTURABLE = TagKey.create(Registries.ENTITY_TYPE, Const.rl("aquatic_capturable"));
    public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = TagKey.create(Registries.ENTITY_TYPE, Const.rl("capturing_not_supported"));

}
