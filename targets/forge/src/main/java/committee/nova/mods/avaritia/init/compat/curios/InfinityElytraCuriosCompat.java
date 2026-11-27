package committee.nova.mods.avaritia.init.compat.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.UUID;

/**
 * 无尽鞘翅的 Curios 背饰槽能力适配。
 *
 * @author HowXu <dev@howxu.cn>
 * @author cnlimiter
 */
public class InfinityElytraCuriosCompat {
    private static final ResourceLocation CAELUS_FALL_FLYING = new ResourceLocation("caelus", "fall_flying");

    public static ICapabilityProvider createProvider(ItemStack stack) {
        return CuriosApi.createCurioProvider(new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                LivingEntity entity = slotContext.entity();
                int ticks = entity.getFallFlyingTicks();
                if (ticks > 0 && entity.isFallFlying()) {
                    stack.elytraFlightTick(entity, ticks);
                }
            }

            @Override
            public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
                Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
                // cnlimiter：Caelus 存在时用它的 fall_flying 属性接入原版鞘翅判定；无 Caelus 的兜底逻辑放在 InfinityElytraUtils。
                Attribute fallFlying = ForgeRegistries.ATTRIBUTES.getValue(CAELUS_FALL_FLYING);
                if (fallFlying != null) {
                    modifiers.put(fallFlying, new AttributeModifier(uuid, "Infinity elytra curio modifier", 1.0D, AttributeModifier.Operation.ADDITION));
                }
                return modifiers;
            }

            @Override
            public boolean canEquip(SlotContext slotContext) {
                return CuriosTools.BACK_SLOT.equals(slotContext.identifier());
            }

            @Override
            public SoundInfo getEquipSound(SlotContext slotContext) {
                return new SoundInfo(SoundEvents.ARMOR_EQUIP_ELYTRA, 1.0F, 1.0F);
            }

            @Override
            public boolean canEquipFromUse(SlotContext slotContext) {
                return CuriosTools.BACK_SLOT.equals(slotContext.identifier());
            }
        });
    }
}
