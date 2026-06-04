package com.avaritia.init.registry;

import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Optional;

/**
 * 注册模组中的所有附魔。
 */
@SuppressWarnings("unused")
public class ModEnchants {

    public static final ResourceKey<Enchantment> FROST_WALKER = key("frost_walker");

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Enchantment> holdergetter1 = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> holdergetter2 = context.lookup(Registries.ITEM);
        register(
                context,
                FROST_WALKER,
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                2,
                                2,
                                Enchantment.dynamicCost(10, 10),
                                Enchantment.dynamicCost(25, 10),
                                4,
                                EquipmentSlotGroup.ANY
                        )
                )
                        .exclusiveWith(holdergetter1.getOrThrow(EnchantmentTags.BOOTS_EXCLUSIVE))
                        .withEffect(
                                EnchantmentEffectComponents.DAMAGE_IMMUNITY,
                                DamageImmunity.INSTANCE,
                                DamageSourceCondition.hasDamageSource(
                                        DamageSourcePredicate.Builder.damageType()
                                                .tag(TagPredicate.is(DamageTypeTags.BURN_FROM_STEPPING))
                                                .tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
                                )
                        )
                        .withEffect(
                                EnchantmentEffectComponents.LOCATION_CHANGED,
                                new ReplaceDisk(
                                        new LevelBasedValue.Clamped(LevelBasedValue.perLevel(3.0F, 1.0F), 0.0F, 16.0F),
                                        LevelBasedValue.constant(1.0F),
                                        new Vec3i(0, -1, 0),
                                        Optional.of(
                                                BlockPredicate.allOf(
                                                        BlockPredicate.matchesTag(new Vec3i(0, 1, 0), BlockTags.AIR),
                                                        BlockPredicate.matchesBlocks(Blocks.WATER),
                                                        BlockPredicate.matchesFluids(Fluids.WATER),
                                                        BlockPredicate.unobstructed()
                                                )
                                        ),
                                        BlockStateProvider.simple(Blocks.FROSTED_ICE),
                                        Optional.of(GameEvent.BLOCK_PLACE)
                                ),
                                LootItemEntityPropertyCondition.hasProperties(
                                        LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(true))
                                )
                        )
        );
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.identifier()));
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace(name));
    }
}
