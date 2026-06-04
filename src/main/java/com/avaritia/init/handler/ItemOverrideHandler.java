package com.avaritia.init.handler;

import com.avaritia.Const;
import com.avaritia.common.item.misc.InfinityUmbrellaItem;
import com.avaritia.common.item.resources.MatterClusterItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ItemOverrideHandler {
    private ItemOverrideHandler() {
    }

    @SubscribeEvent
    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(Const.rl("mode_flag"), ModeFlag.MAP_CODEC);
        event.register(Const.rl("umbrella_mode"), UmbrellaMode.MAP_CODEC);
        event.register(Const.rl("matter_cluster_full"), MatterClusterFull.MAP_CODEC);
        event.register(Const.rl("using_stack"), UsingStack.MAP_CODEC);
        event.register(Const.rl("infinity_crossbow_pull"), InfinityCrossbowPull.MAP_CODEC);
        event.register(Const.rl("infinity_crossbow_charged"), InfinityCrossbowCharged.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Const.rl("mode_flag"), ModeFlag.MAP_CODEC);
        event.register(Const.rl("matter_cluster_full"), MatterClusterFull.MAP_CODEC);
        event.register(Const.rl("using_stack"), UsingStack.MAP_CODEC);
        event.register(Const.rl("infinity_crossbow_charged"), InfinityCrossbowCharged.MAP_CODEC);
    }

    public record ModeFlag(String key) implements RangeSelectItemModelProperty, ConditionalItemModelProperty {
        public static final MapCodec<ModeFlag> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("key").forGetter(ModeFlag::key)
        ).apply(instance, ModeFlag::new));

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return this.enabled(stack) ? 1.0F : 0.0F;
        }

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
            return this.enabled(stack);
        }

        @Override
        public MapCodec<ModeFlag> type() {
            return MAP_CODEC;
        }

        private boolean enabled(ItemStack stack) {
            return modeTag(stack).getBoolean(this.key).orElse(false);
        }
    }

    public record UmbrellaMode() implements RangeSelectItemModelProperty {
        public static final MapCodec<UmbrellaMode> MAP_CODEC = MapCodec.unit(new UmbrellaMode());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            CompoundTag mode = modeTag(stack);
            for (int i = 0; i < InfinityUmbrellaItem.MODES.size(); i++) {
                if (mode.getBoolean(InfinityUmbrellaItem.MODES.get(i)).orElse(false)) {
                    return i;
                }
            }
            return 0.0F;
        }

        @Override
        public MapCodec<UmbrellaMode> type() {
            return MAP_CODEC;
        }
    }

    public record MatterClusterFull() implements RangeSelectItemModelProperty, ConditionalItemModelProperty {
        public static final MapCodec<MatterClusterFull> MAP_CODEC = MapCodec.unit(new MatterClusterFull());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return this.full(stack) ? 1.0F : 0.0F;
        }

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
            return this.full(stack);
        }

        @Override
        public MapCodec<MatterClusterFull> type() {
            return MAP_CODEC;
        }

        private boolean full(ItemStack stack) {
            return MatterClusterItem.getClusterSize(MatterClusterItem.getClusterItems(stack)) == MatterClusterItem.CAPACITY;
        }
    }

    public record UsingStack() implements RangeSelectItemModelProperty, ConditionalItemModelProperty {
        public static final MapCodec<UsingStack> MAP_CODEC = MapCodec.unit(new UsingStack());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return this.using(stack, entity) ? 1.0F : 0.0F;
        }

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
            return this.using(stack, entity);
        }

        @Override
        public MapCodec<UsingStack> type() {
            return MAP_CODEC;
        }

        private boolean using(ItemStack stack, @Nullable LivingEntity entity) {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack;
        }
    }

    public record InfinityCrossbowPull() implements RangeSelectItemModelProperty {
        public static final MapCodec<InfinityCrossbowPull> MAP_CODEC = MapCodec.unit(new InfinityCrossbowPull());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            if (entity == null || CrossbowItem.isCharged(stack)) {
                return 0.0F;
            }
            return (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
        }

        @Override
        public MapCodec<InfinityCrossbowPull> type() {
            return MAP_CODEC;
        }
    }

    public record InfinityCrossbowCharged() implements RangeSelectItemModelProperty, ConditionalItemModelProperty {
        public static final MapCodec<InfinityCrossbowCharged> MAP_CODEC = MapCodec.unit(new InfinityCrossbowCharged());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
        }

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
            return CrossbowItem.isCharged(stack);
        }

        @Override
        public MapCodec<InfinityCrossbowCharged> type() {
            return MAP_CODEC;
        }
    }

    private static CompoundTag modeTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag().getCompound("mode").orElseGet(CompoundTag::new);
    }
}
